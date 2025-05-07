package com.gts.fichaweb.views;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import modelos.Usuario;
import repositorios.UsuarioRepositorio;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Route("admin")
public class AdminVista extends VerticalLayout{

	private final UsuarioRepositorio usuarioRepositorio;

    @Autowired
    public AdminVista(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;

        Image logo = new Image("img/icono.png", "Logo");
        logo.setWidth("60px");
        logo.setHeight("60px");

        H1 titulo = new H1("/Admin");
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
        	}
        	if(usuario.getRol() == 0 || usuario.getRol() == 1) {
        		VaadinSession.getCurrent().setAttribute("username", usuario.getLoginUsuario());
        		Notification.show("Bienvenido, " + usuario.getNombre(), 2000, Notification.Position.TOP_CENTER);
                UI.getCurrent().access(() -> {
                    UI.getCurrent().navigate("listusuarios");
                });
        	} else {
        		Notification.show("Usuario no administrador", 2000, Notification.Position.TOP_CENTER);
        	}
        } else {
            Notification.show("Usuario o contraseña incorrectos", 2000, Notification.Position.TOP_CENTER);
        }
    }
}