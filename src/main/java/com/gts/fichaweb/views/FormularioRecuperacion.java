package com.gts.fichaweb.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import modelos.PasswordToken;
import modelos.Usuario;
import repositorios.UsuarioRepositorio;
import repositorios.PasswordTokenRepositorio;
import servicios.EmailServicio;

import org.springframework.beans.factory.annotation.Autowired;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Route("recuperar")
@PermitAll
public class FormularioRecuperacion extends VerticalLayout {
    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordTokenRepositorio passwordTokenRepo;
    private final EmailServicio emailServicio;

    @Autowired
    public FormularioRecuperacion(UsuarioRepositorio usuarioRepositorio, PasswordTokenRepositorio passwordTokenRepo, EmailServicio emailServicio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordTokenRepo = passwordTokenRepo;
        this.emailServicio = emailServicio;

        Image logo = new Image("img/icono.png", "Logo");
        logo.setWidth("60px");
        logo.setHeight("60px");

        H1 titulo = new H1("Recuperación");
        titulo.getStyle().set("font-size", "40px").set("text-align", "center");

        HorizontalLayout headerLayout = new HorizontalLayout(logo, titulo);
        headerLayout.setAlignItems(Alignment.CENTER);

        TextField emailField = new TextField("Correo Electrónico");
        emailField.setWidth("300px");

        Button btnRecuperar = new Button("Enviar");
        btnRecuperar.setWidth("300px");
        btnRecuperar.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer");
        
        Anchor linkRetroceder = new Anchor("", "Volver");
        linkRetroceder.getStyle().set("font-size", "14px").set("color", "#007BFF").set("cursor", "pointer");

        VerticalLayout formLayout = new VerticalLayout(headerLayout, emailField, btnRecuperar, linkRetroceder);
        formLayout.setAlignItems(Alignment.CENTER);
        formLayout.setSpacing(true);

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        add(formLayout);

        btnRecuperar.addClickListener(event -> {
            String email = emailField.getValue().trim();
            if (!email.isEmpty()) {
            	crearToken(email);
                btnRecuperar.setText("Enviado ✔");
                btnRecuperar.setEnabled(false);
            }
        });
    }

    
    private void crearToken(String email) {
        Optional<Usuario> usuario = usuarioRepositorio.findByEmail(email);
        if (usuario.isEmpty()) return;

        String token = generarToken(); 
        LocalDateTime tiempo = LocalDateTime.now().plusMinutes(30);

        PasswordToken pt = new PasswordToken();
        pt.setToken(token);
        pt.setEmail(email);
        pt.setExpiration(tiempo);

        passwordTokenRepo.save(pt);
        emailServicio.enviarCorreo(email, token);  
    }

    private String generarToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[24]; 
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes); 
    }
}
