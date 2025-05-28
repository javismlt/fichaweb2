package com.gts.fichaweb.views;

import modelos.Usuario;
import com.vaadin.flow.component.Key;
import repositorios.UsuarioRepositorio;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.vaadin.flow.component.html.Anchor;

@Route("")
public class LoginVista extends VerticalLayout {

    private final UsuarioRepositorio usuarioRepositorio;

    @Autowired
    public LoginVista(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;

        Image logo = new Image("img/icono.png", "Logo");
        logo.setWidth("60px");
        logo.setHeight("60px");

        H1 titulo = new H1("FichaWeb");
        titulo.getStyle().set("font-size", "40px").set("text-align", "center");

        HorizontalLayout headerLayout = new HorizontalLayout(logo, titulo);
        headerLayout.setAlignItems(Alignment.CENTER);

        TextField usuarioField = new TextField("Usuario");
        usuarioField.setWidth("300px");

        PasswordField passwordField = new PasswordField("Contraseña");
        passwordField.setWidth("300px");

        Button btnLogin = new Button("Iniciar sesión");
        btnLogin.setWidth("300px");
        btnLogin.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer");
        btnLogin.addClickShortcut(Key.ENTER);
        
        Anchor linkRecuperacion = new Anchor("recuperar", "¿Olvidaste tu contraseña?");
        linkRecuperacion.getStyle().set("font-size", "14px").set("color", "#007BFF").set("cursor", "pointer");

        VerticalLayout formLayout = new VerticalLayout(headerLayout, usuarioField, passwordField, btnLogin, linkRecuperacion);
        formLayout.setAlignItems(Alignment.CENTER);
        formLayout.setSpacing(true);

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        add(formLayout);

        btnLogin.addClickListener(event -> {
        	String username = usuarioField.getValue().trim();
            String password = passwordField.getValue().trim();
            autenticarUsuario(username, password);
        });
    }

    
    private void autenticarUsuario(String username, String password) {
        Usuario usuario = usuarioRepositorio.findByLoginUsuario(username);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        if (usuario != null && passwordEncoder.matches(password, usuario.getPassword())) {
        	if(usuario.getActivo() == 0) {
        		Notification.show("Usuario desactivado", 2000, Notification.Position.TOP_CENTER);
        		return;
        	} else if(usuario.getEmpresa().getActivo() == 0)  {
        		Notification.show("Empresa desactivada", 2000, Notification.Position.TOP_CENTER);
        		return;
        	} else if(usuario.getRol() == 1 || usuario.getRol() == 2 || usuario.getRol() == 5)  {
        		Notification.show("Acceso denegado", 2000, Notification.Position.TOP_CENTER);
        		return;
        	}
            VaadinSession.getCurrent().setAttribute("username", usuario.getLoginUsuario());
            Notification.show("Bienvenido, " + usuario.getNombre(), 2000, Notification.Position.TOP_CENTER);
            UI.getCurrent().access(() -> {
                UI.getCurrent().navigate("registro");
            });
        } else {
            Notification.show("Usuario o contraseña incorrectos", 2000, Notification.Position.TOP_CENTER);
        }
    }
}