package com.gts.fichaweb.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import repositorios.UsuarioRepositorio;
import repositorios.RegistroRepositorio;
import modelos.Usuario;
import modelos.Notificaciones;
import modelos.Permisos;
import modelos.Registro;
import modelos.Solicitudes;
import modelos.Notificaciones;
import modelos.Solicitudes;
import modelos.Permisos;
import repositorios.NotificacionesRepositorio;
import repositorios.SolicitudesRepositorio;
import repositorios.PermisosRepositorio;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.textfield.TextField;

@Route("olvidado")
@CssImport(value = "./themes/my-theme/styles.css", themeFor = "vaadin-grid")
public class FichajeOlvidado extends AppLayout{
    private final UsuarioRepositorio usuarioRepositorio;
    private final RegistroRepositorio registroRepositorio;
    private final NotificacionesRepositorio notificacionesRepositorio; 
    private final PermisosRepositorio permisosRepositorio; 
    private final SolicitudesRepositorio solicitudesRepositorio; 
    private Usuario usuarioActual;
    private Usuario usuarioActualAux;
    private String usuario1;
    private String accionAux;
    private String nombreUsuario;
    
    public FichajeOlvidado(UsuarioRepositorio usuarioRepositorio, RegistroRepositorio registroRepositorio, NotificacionesRepositorio notificacionesRepositorio, PermisosRepositorio permisosRepositorio, SolicitudesRepositorio solicitudesRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.registroRepositorio = registroRepositorio;
        this.notificacionesRepositorio = notificacionesRepositorio;
        this.permisosRepositorio = permisosRepositorio;
        this.solicitudesRepositorio = solicitudesRepositorio;
        
        nombreUsuario = (String) VaadinSession.getCurrent().getAttribute("username");
        if (nombreUsuario == null) {
            getElement().executeJs("window.location.href='/'");
        } else {
        	if (VaadinSession.getCurrent().getAttribute("UserManual") == null) {
        		usuarioActualAux = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
        		usuario1 = usuarioActualAux.getLoginUsuario();
        	} else {
        		usuario1 = (String) VaadinSession.getCurrent().getAttribute("UserManual");
        	}
            crearHeader(nombreUsuario);
            cargarMenu();
        }
    }

    private void crearHeader(String nombreUsuario) {
        Anchor registro = new Anchor("registro", "Registro");
        registro.getElement().setAttribute("href", "/registro");
        registro.getStyle().set("color", "black").set("text-decoration", "none").set("font-size", "16px");
        
        Anchor consulta = new Anchor("consulta", "Consulta");
        consulta.getElement().setAttribute("href", "/consulta");
        consulta.getStyle().set("color", "black").set("text-decoration", "none").set("font-size", "16px");

        Anchor permisos = new Anchor("permisos", "Permisos");
        permisos.getElement().setAttribute("href", "/permisos");
        permisos.getStyle().set("color", "black").set("text-decoration", "none").set("font-size", "16px");
        
        usuarioActual = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
        if (usuarioActual != null && usuarioActual.getRol() == 3) {
            permisos.setVisible(false);
        }
        
        HorizontalLayout menuIzquierdo = new HorizontalLayout(registro, consulta, permisos);
        menuIzquierdo.setSpacing(true);
        menuIzquierdo.getStyle().set("gap", "25px");
        menuIzquierdo.setAlignItems(Alignment.CENTER);
        
        Button menuDesplegable = new Button("☰"); 
        menuDesplegable.getStyle().set("font-size", "24px").set("background", "none").set("border", "1px solid black").set("cursor", "pointer").set("border-radius", "4px").set("display", "none");

        ContextMenu menuResponsive = new ContextMenu(menuDesplegable);
        menuResponsive.setOpenOnClick(true);
        menuResponsive.addItem("Registro", e -> UI.getCurrent().navigate("registro"));
        menuResponsive.addItem("Consulta", e -> UI.getCurrent().navigate("consulta"));
        menuResponsive.addItem("Permisos", e -> UI.getCurrent().navigate("permisos"));
        MenuItem itemPermisos = menuResponsive.addItem("Permisos", e -> UI.getCurrent().navigate("permisos"));
        if (usuarioActual != null && usuarioActual.getRol() == 3) {
            itemPermisos.setVisible(false);
        }
        
        menuIzquierdo.getElement().getClassList().add("menu-izquierdo");
        menuDesplegable.getElement().getClassList().add("menu-desplegable");
        
        Usuario usuario = usuarioRepositorio.findByLoginUsuario(usuario1);
        int notificacionesPendientes = notificacionesRepositorio.countByEstadoAndSolicitanteIdAndResolucionNotNull(1, usuario.getId());
        
        Image correoImagen = new Image("img/correo.png", "Correo Icono");
        correoImagen.setWidth("35px");
        correoImagen.setHeight("35px");
        correoImagen.getStyle().set("margin-top", "8px");
        
        Button btnNotificaciones = new Button(correoImagen);
        btnNotificaciones.addClickListener(e -> mostrarDialogoNotificaciones());
        btnNotificaciones.getStyle().set("font-size", "20px").set("background", "transparent").set("border", "none").set("cursor", "pointer").set("position", "relative");

        Div notificacionesWrapper = new Div();
        notificacionesWrapper.getStyle().set("position", "relative").set("display", "inline-block");
        notificacionesWrapper.getElement().getClassList().add("btn-notificaciones");
        notificacionesWrapper.add(btnNotificaciones);

        if (notificacionesPendientes > 0) {
            Span badge = new Span(String.valueOf(notificacionesPendientes));
            badge.getStyle().set("background-color", "red").set("color", "white").set("border-radius", "50%").set("width", "18px").set("height", "18px").set("font-size", "14px").set("position", "absolute").set("top", "-5px").set("right", "-5px").set("display", "flex").set("align-items", "center").set("justify-content", "center");
            notificacionesWrapper.add(badge);
        }
        
        Button menuUser = new Button(nombreUsuario);
        menuUser.getStyle().set("color", "black").set("font-size", "16px").set("cursor", "pointer").set("border", "1px solid black").set("border-radius", "4px");

        Div menuUserWrapper = new Div();
        menuUserWrapper.getStyle().set("position", "relative").set("display", "inline-block");
        menuUserWrapper.add(menuUser);

        if (notificacionesPendientes > 0) {
            Span mobileBadge = new Span(String.valueOf(notificacionesPendientes));
            mobileBadge.addClassName("mobile-badge");
            mobileBadge.getStyle().set("background-color", "red").set("color", "white").set("border-radius", "50%").set("width", "16px").set("height", "16px").set("font-size", "12px").set("position", "absolute").set("top", "-5px").set("right", "-5px").set("display", "flex").set("align-items", "center").set("justify-content", "center");
            menuUserWrapper.add(mobileBadge);
        }

        ContextMenu contextMenu = new ContextMenu(menuUser);
        contextMenu.setOpenOnClick(true);
        contextMenu.addItem("Notificaciones (" + notificacionesPendientes + ")", e -> mostrarDialogoNotificaciones());
        contextMenu.addItem("Cerrar sesión", e -> {
            UI.getCurrent().access(() -> {
                VaadinSession.getCurrent().close();
                UI.getCurrent().access(() -> {
                    UI.getCurrent().navigate("");
                });
            });
        });

        HorizontalLayout menuDerecho = new HorizontalLayout();
        menuDerecho.add(notificacionesWrapper, menuUserWrapper);
        menuDerecho.setSpacing(true);
        menuDerecho.getStyle().set("gap", "25px");
        menuDerecho.setAlignItems(Alignment.CENTER);

        HorizontalLayout header = new HorizontalLayout(menuIzquierdo, menuDesplegable, menuDerecho);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        header.setPadding(true);
        header.getStyle().set("padding-top", "10px").set("padding-bottom", "10px").set("padding-left", "100px").set("padding-right", "100px").set("background-color", "#f8f9fa").set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)");

        addToNavbar(header);
    }
    
    private void cargarMenu() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setAlignItems(Alignment.CENTER);

        DatePicker fechaPicker = new DatePicker("Fecha");
        fechaPicker.setWidth("200px");
        
        TimePicker hora = new TimePicker("Hora");
        hora.setStep(Duration.ofMinutes(15));
        hora.setWidth("200px");

        Select<String> accionSelect = new Select<>();
        accionSelect.setLabel("Acción");
        accionSelect.setItems("ENTRADA", "PAUSA", "RETORNO", "SALIDA");
        accionSelect.setPlaceholder("Selecciona una acción");
        accionSelect.setWidth("200px");
        
        TextField comentarioField = new TextField("Observaciones");
        comentarioField.setMaxLength(144);
        comentarioField.setPlaceholder("Introduce un comentario (máx. 144 caracteres)");
        comentarioField.setWidth("200px");

        Button btnguardar = new Button("Fichar");
        btnguardar.setWidth("100px");
        btnguardar.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer");

        btnguardar.addClickListener(event -> {
            LocalDate fecha = fechaPicker.getValue();
            LocalTime horaInicio = hora.getValue();

            if (fecha == null || horaInicio == null || accionSelect == null) {
                Notification.show("Por favor completa todos los campos", 2000, Notification.Position.TOP_CENTER);
                return;
            }

            String accion = accionSelect.getValue();
            String observaciones;
        
            if ("ENTRADA".equals(accion)) {
            	accionAux = "Entrada";
            } else if ("PAUSA".equals(accion)) {
            	accionAux = "Pausa";
            } else if ("RETORNO".equals(accion)) {
            	accionAux = "Retorno";
            } else if ("SALIDA".equals(accion)) {
            	accionAux = "Salida";
            }
            if(comentarioField.getValue() != null && !comentarioField.getValue().trim().isEmpty()) {
            	observaciones = comentarioField.getValue();
            } else {
            	observaciones = accionAux + " fichaje manual";
            }
            
            if ("ENTRADA".equals(accion)) {
            	registrarFichajeEntrada(usuario1, fecha, horaInicio, observaciones);
            } else if ("PAUSA".equals(accion)) {
            	registrarPausa(usuario1, fecha, horaInicio, observaciones);
            } else if ("RETORNO".equals(accion)) {
            	registrarReanudacion(usuario1, fecha, horaInicio, observaciones);
            } else if ("SALIDA".equals(accion)) {
            	registrarFichajeSalida(usuario1, fecha, horaInicio, observaciones);
            }
            
            Notification.show("Registro guardado", 2000, Notification.Position.TOP_CENTER);
            UI.getCurrent().navigate("registro");
        });

        layout.add(fechaPicker, hora, accionSelect, comentarioField, btnguardar);
        setContent(layout);
    }
    

    private void registrarFichajeEntrada (String nombreUsuario, LocalDate fecha, LocalTime horaInicio, String observaciones) {
        Usuario usuario = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
        if (usuario != null) {
            Registro registro = new Registro();
            registro.setUsuarioId(usuario);  
            registro.setFechaRegistro(fecha);
            registro.setHora(horaInicio);
            registro.setAccion("ENTRADA");
            registro.setObservaciones(observaciones);
            registro.setOrigen("N");
            registro.setValidado(0);
            
            registroRepositorio.save(registro);
            cargarMenu(); 
        }
    }
    
    private void registrarFichajeSalida (String nombreUsuario, LocalDate fecha, LocalTime horaFin, String observaciones) {
        Usuario usuario = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
        Registro ultimoRegistro = registroRepositorio.findTopByUsuarioAndAccionOrderByIdDesc(usuario, "ENTRADA");
        Integer idUltimaEntrada = ultimoRegistro != null ? ultimoRegistro.getId() : null;
        if (usuario != null) {
            Registro registro = new Registro();
            registro.setUsuarioId(usuario);  
            registro.setFechaRegistro(fecha);
            registro.setHora(horaFin);
            registro.setAccion("SALIDA");
            registro.setObservaciones(observaciones);
            registro.setOrigen("N");
            registro.setValidado(0);
            registro.setIdAsociado(null);
            
            registroRepositorio.save(registro);
            cargarMenu(); 
        }
    }
    
    private void registrarPausa(String nombreUsuario, LocalDate fecha, LocalTime horaPausa, String observaciones) {
    	Usuario usuario = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
    	Registro ultimoRegistro = registroRepositorio.findTopByUsuarioAndAccionOrderByIdDesc(usuario, "ENTRADA");
        Integer idUltimaEntrada = ultimoRegistro != null ? ultimoRegistro.getId() : null;
    	if (idUltimaEntrada != null) {
            Registro registro = new Registro();
            registro.setUsuarioId(usuario);  
            registro.setFechaRegistro(fecha);
            registro.setHora(horaPausa);
            registro.setAccion("PAUSA");
            registro.setObservaciones(observaciones);
            registro.setOrigen("N");
            registro.setValidado(0);
            registro.setIdAsociado(null);
     
            registroRepositorio.save(registro);
            cargarMenu(); 
        }
    }
    
    private void registrarReanudacion(String nombreUsuario, LocalDate fecha, LocalTime horaReanudacion, String observaciones) {
    	Usuario usuario = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
    	Registro ultimoRegistro = registroRepositorio.findTopByUsuarioAndAccionOrderByIdDesc(usuario, "PAUSA");
        Integer idUltimaPausa = ultimoRegistro != null ? ultimoRegistro.getId() : null;
    	if (idUltimaPausa != null) {
            Registro registro = new Registro();
            registro.setUsuarioId(usuario);  
            registro.setFechaRegistro(fecha);
            registro.setHora(horaReanudacion);
            registro.setAccion("RETORNO");
            registro.setObservaciones(observaciones);
            registro.setOrigen("N");
            registro.setValidado(0);
            registro.setIdAsociado(null);
     
            registroRepositorio.save(registro);
            cargarMenu(); 
        }
    }
    
    private void mostrarDialogoNotificaciones() {
    	Dialog dialog = new Dialog();
        dialog.setWidth("550px");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        Span titulo = new Span("Notificaciones");
        titulo.getStyle().set("font-weight", "bold").set("font-size", "18px");
        layout.add(titulo);

        Usuario usuario = usuarioRepositorio.findByLoginUsuario(usuario1);
        List<Notificaciones> pendientes = notificacionesRepositorio.findByEstadoAndSolicitanteIdAndResolucionNotNull(1, usuario.getId());

        if (pendientes.isEmpty()) {
            Span sinNotificaciones = new Span("No hay notificaciones");
            sinNotificaciones.getStyle().set("font-style", "italic").set("color", "#666").set("padding", "10px");
            layout.add(sinNotificaciones);
        } else {
            for (Notificaciones notificacion : pendientes) {
                HorizontalLayout tarjeta = new HorizontalLayout();
                tarjeta.setWidthFull();
                tarjeta.setJustifyContentMode(JustifyContentMode.BETWEEN);
                tarjeta.getStyle().set("border", "1px solid #ccc").set("border-radius", "4px").set("box-shadow", "0 2px 5px rgba(0, 0, 0, 0.1)");

                Span texto1 = new Span();
                Span texto2 = new Span();
                
                if ("PERMISO".equals(notificacion.getTipo())) {
                	Optional<Permisos> permisoOptional = permisosRepositorio.findById(notificacion.getidAsociado());
                	Permisos permisoNotificacion = permisoOptional.get();
                	texto1 = new Span(permisoNotificacion.getMotivo() + " - " + notificacion.getResolucion());
                	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                	if (permisoNotificacion.getFechaAux() == null) {
                		texto2 = new Span(permisoNotificacion.getFecha().format(formatter));
                	} else {
                		texto2 = new Span(permisoNotificacion.getFecha().format(formatter) + " - " + permisoNotificacion.getFechaAux().format(formatter));
                	}
                } else {
                	Optional<Solicitudes> solicitudesOptional = solicitudesRepositorio.findById(notificacion.getidAsociado());
                	Solicitudes solicitudNotificacion = solicitudesOptional.get();
                	texto1 = new Span(solicitudNotificacion.getTipo() + " - " + notificacion.getResolucion());
                	texto2 = new Span(solicitudNotificacion.getRegistro().getFechaRegistro() + " " + solicitudNotificacion.getRegistro().getAccion() + ", "+ solicitudNotificacion.getValorPrevio() + " a " + solicitudNotificacion.getValor());
                }
                
                texto1.getStyle().set("font-weight", "500");
                texto2.getStyle().set("font-weight", "500");
                
                Image rechazarImagen = new Image("img/no.png", "Rechazar Icono");
                rechazarImagen.setWidth("45px");
                rechazarImagen.setHeight("45px");
                rechazarImagen.getStyle().set("margin-top", "5px");
                Button rechazar = new Button(rechazarImagen, ev -> {
                	 notificacion.setEstado(2);
                     notificacionesRepositorio.save(notificacion);
                     dialog.close(); 
                     UI.getCurrent().getPage().reload();
                });
                rechazar.getStyle().set("background-color", "white").set("cursor", "pointer");

                VerticalLayout textos = new VerticalLayout(texto1, texto2);
                textos.setSpacing(false);
                textos.setPadding(false);
                textos.setMargin(false);
                textos.getStyle().set("padding", "5px").set("margin", "5px");
                HorizontalLayout botones = new HorizontalLayout(rechazar);
                botones.setSpacing(false);
                botones.getStyle().set("margin-top", "15px");
                tarjeta.add(textos, botones);
                layout.add(tarjeta);
            }
        }
        dialog.add(layout);
        dialog.open();
    }
}