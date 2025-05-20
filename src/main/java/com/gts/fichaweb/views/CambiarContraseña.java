package com.gts.fichaweb.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import modelos.PasswordToken;
import modelos.Usuario;
import repositorios.PasswordTokenRepositorio;
import repositorios.UsuarioRepositorio;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Route("cambiarpasswd")
public class CambiarContraseña extends AppLayout implements BeforeEnterObserver {
    private final PasswordTokenRepositorio passwordTokenRepo;
    private final UsuarioRepositorio usuarioRepositorio;
    private Usuario usuarioActual;

    public CambiarContraseña(PasswordTokenRepositorio passwordTokenRepo, UsuarioRepositorio usuarioRepositorio) {
        this.passwordTokenRepo = passwordTokenRepo;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String token = event.getLocation().getQueryParameters().getParameters().getOrDefault("token", List.of("")).stream().findFirst().orElse(null);

        if (token == null || token.isEmpty()) {
        	Notification.show("Url no valida", 2000, Notification.Position.TOP_CENTER);
            return;
        }

        Optional<PasswordToken> tokenOpt = passwordTokenRepo.findByToken(token);
        if (tokenOpt.isEmpty() || tokenOpt.get().getExpiration().isBefore(LocalDateTime.now())) {
            Notification.show("Tiempo de recuperación expirado", 2000, Notification.Position.TOP_CENTER);
            return;
        }

        PasswordToken passwordToken = tokenOpt.get();
        String email = passwordToken.getEmail();

        Optional<Usuario> user = usuarioRepositorio.findByEmail(email);
        if (user.isEmpty()) {
        	Notification.show("Ha ocurrido un error", 2000, Notification.Position.TOP_CENTER);
            return;
        }

        Usuario usuario = user.get();

        if (usuario == null) {
            return;
        }

        this.usuarioActual = usuario;
        crearFormulario();
    }

    private void crearFormulario() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.setSizeFull();
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        layout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);

        PasswordField nuevaPassword = new PasswordField("Nueva contraseña");
        PasswordField confirmarPassword = new PasswordField("Confirmar contraseña");
        
        nuevaPassword.setWidth("300px");
        confirmarPassword.setWidth("300px");

        Button btnconfirmar = new Button("Cambiar contraseña", e -> {
            String nueva = nuevaPassword.getValue();
            String confirmar = confirmarPassword.getValue();

            if (nueva == null || confirmar == null || nueva.isEmpty() || confirmar.isEmpty()) {
            	Notification.show("Complete los campos", 2000, Notification.Position.TOP_CENTER);
                return;
            }

            if (!nueva.equals(confirmar)) {
            	Notification.show("Las contraseñas no coinciden", 2000, Notification.Position.TOP_CENTER);
                return;
            }

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            usuarioActual.setPassword(encoder.encode(nueva));
            usuarioRepositorio.save(usuarioActual);

            Notification.show("Contraseña actualizada", 2000, Notification.Position.TOP_CENTER);
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        
        btnconfirmar.setWidth("300px");
        btnconfirmar.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer");

        Image logo = new Image("img/icono.png", "Logo");
        logo.setWidth("60px");
        logo.setHeight("60px");

        H1 titulo = new H1("Recuperación");
        titulo.getStyle().set("font-size", "40px").set("text-align", "center");

        HorizontalLayout headerLayout = new HorizontalLayout(logo, titulo);
        headerLayout.setAlignItems(Alignment.CENTER);
        layout.add(headerLayout, nuevaPassword, confirmarPassword, btnconfirmar);
        setContent(layout);
    }
}