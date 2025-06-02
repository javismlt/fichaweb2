package com.gts.fichaweb.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import java.time.format.TextStyle;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.applayout.AppLayout;
import modelos.Usuario;
import modelos.Registro;
import modelos.Permisos;
import modelos.Solicitudes;
import modelos.Notificaciones;
import repositorios.RegistroRepositorio;
import repositorios.PermisosRepositorio;
import repositorios.UsuarioRepositorio;
import servicios.EmailNotificacion;
import repositorios.NotificacionesRepositorio;
import repositorios.SolicitudesRepositorio;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import java.time.LocalDate;
import java.time.LocalTime;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.YearMonth;
import java.time.DayOfWeek;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import java.time.format.DateTimeFormatter;
import modelos.Notificaciones;
import modelos.Solicitudes;
import modelos.Permisos;
import repositorios.NotificacionesRepositorio;
import repositorios.SolicitudesRepositorio;
import repositorios.PermisosRepositorio;
import servicios.EmailNotificacion;

@Route("permisos")
@CssImport(value = "./themes/my-theme/styles.css", themeFor = "vaadin-grid")
public class PermisosVista extends AppLayout {
    private final UsuarioRepositorio usuarioRepositorio;
    private final PermisosRepositorio permisosRepositorio; 
    private final RegistroRepositorio registroRepositorio;  
    private final NotificacionesRepositorio notificacionesRepositorio; 
    private final SolicitudesRepositorio solicitudesRepositorio;
    private Usuario usuarioActual;
    private YearMonth currentMonth = YearMonth.now();
    private VerticalLayout contenido; 
    private final EmailNotificacion emailNotificacion;

    public PermisosVista(UsuarioRepositorio usuarioRepositorio, RegistroRepositorio registroRepositorio, PermisosRepositorio permisosRepositorio, NotificacionesRepositorio notificacionesRepositorio, SolicitudesRepositorio solicitudesRepositorio, EmailNotificacion emailNotificacion) {
        this.registroRepositorio = registroRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.permisosRepositorio = permisosRepositorio; 
        this.notificacionesRepositorio = notificacionesRepositorio; 
        this.solicitudesRepositorio = solicitudesRepositorio;
        this.emailNotificacion = emailNotificacion;
        
        String nombreUsuario = (String) VaadinSession.getCurrent().getAttribute("username");
        if (nombreUsuario == null) {
            getElement().executeJs("window.location.href='/'");
        } else {
            this.usuarioActual = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
            crearHeader(nombreUsuario);
            crearContenido();
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

        menuIzquierdo.getElement().getClassList().add("menu-izquierdo");
        menuDesplegable.getElement().getClassList().add("menu-desplegable");
        
        int notificacionesPendientes = notificacionesRepositorio.countByEstadoAndSolicitanteIdAndResolucionNotNull(1, usuarioActual.getId());
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

    private void crearContenido() {
        contenido = new VerticalLayout(); 
        contenido.setSizeFull();
        contenido.setPadding(true);
        contenido.setSpacing(true);
        contenido.setAlignItems(Alignment.CENTER);

        contenido.add(crearCabecera(currentMonth));
        contenido.add(crearCalendario(currentMonth));

        setContent(contenido);
    }

    
    private HorizontalLayout crearCabecera(YearMonth mes) {
    	Image flechaizqImagen = new Image("img/flechaizq.png", "FlechaIzq Icono");
    	Image flechaderImagen = new Image("img/flechader.png", "FlechaDer Icono");
    	
    	Image[] imagenes = {flechaizqImagen, flechaderImagen};
        for (Image imagen : imagenes) {
            imagen.setWidth("35px");
            imagen.setHeight("35px");
            imagen.getStyle().set("margin-top", "8px");
        }
        Button anterior = new Button("", flechaizqImagen, e -> cambiarMes(-1));
        Button siguiente = new Button("", flechaderImagen, e -> cambiarMes(1));
        Button[] botones = {anterior, siguiente};
        for (Button boton : botones) {
            boton.getStyle().set("background-color", "white").set("cursor", "pointer");
        }
        Span mesActual = new Span(mes.getMonth().getDisplayName(TextStyle.FULL, new Locale("es")) + " " + mes.getYear());
        mesActual.getStyle().set("font-weight", "bold").set("font-size", "20px");

        HorizontalLayout header = new HorizontalLayout(anterior, mesActual, siguiente);
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.CENTER);
        header.setWidthFull();
        return header;
    }

    private VerticalLayout crearCalendario(YearMonth mes) {
        VerticalLayout calendarioCompleto = new VerticalLayout();
        calendarioCompleto.setPadding(false);
        calendarioCompleto.setSpacing(false);
        calendarioCompleto.setWidthFull();
        calendarioCompleto.getStyle().set("display", "flex").set("flex-direction", "column").set("align-items", "center");

        Div cabeceraDias = new Div();
        cabeceraDias.getStyle().set("display", "grid").set("grid-template-columns", "repeat(7, 120px)").set("gap", "5px").set("text-align", "center");
        cabeceraDias.setWidth("840px");
        cabeceraDias.addClassName("cabecera-dias");
        
        for (DayOfWeek day : DayOfWeek.values()) {
            Span label = new Span(day.getDisplayName(TextStyle.SHORT, new Locale("es")));
            label.getStyle().set("font-weight", "bold").set("display", "flex").set("align-items", "center").set("justify-content", "center").set("height", "30px");
            cabeceraDias.add(label);
        }

        Div calendarGrid = new Div();
        calendarGrid.getStyle().set("display", "grid").set("grid-template-columns", "repeat(7, 120px)").set("grid-auto-rows", "120px").set("gap", "5px");
        calendarGrid.setWidth("840px");
        calendarGrid.addClassName("calendar-grid");
        
        LocalDate firstDay = mes.atDay(1);
        int offset = firstDay.getDayOfWeek().getValue() - 1;
        LocalDate inicioDia = firstDay.minusDays(offset);
        LocalDate finDia = mes.atEndOfMonth().plusDays(42 - offset - mes.lengthOfMonth());

        List<Permisos> eventos = obtenerPermisos();

        LocalDate actual = inicioDia;
        while (!actual.isAfter(finDia)) {
            LocalDate finalActual = actual;
            List<Permisos> delDia = eventos.stream()
                .filter(ev -> !finalActual.isBefore(ev.getFecha()) &&
                             !finalActual.isAfter(ev.getFechaAux() != null ? ev.getFechaAux() : ev.getFecha()))
                .collect(Collectors.toList());

            calendarGrid.add(crearCeldaDia(finalActual, delDia, mes));
            actual = actual.plusDays(1);
        }
        calendarioCompleto.add(cabeceraDias, calendarGrid);
        return calendarioCompleto;
    }

    private Div crearCeldaDia(LocalDate fecha, List<Permisos> eventos, YearMonth mesActual) {
        Div dia = new Div();
        dia.getStyle().set("border", "1px solid #ccc").set("padding", "5px").set("min-height", "100px").set("width", "100%").set("box-sizing", "border-box").set("border-radius", "5px").set("display", "flex").set("flex-direction", "column").set("cursor", "pointer"); 

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String fechaFormateada = fecha.format(formatter);
        
        if (!YearMonth.from(fecha).equals(mesActual)) {
            dia.getStyle().set("background-color", "#f0f0f0");
        }

        if (fecha.equals(LocalDate.now())) {
            dia.getStyle().set("border", "2px solid black");
        }

        Span numero = new Span(String.valueOf(fecha.getDayOfMonth()));
        numero.getStyle().set("font-weight", "bold");
        dia.add(numero);

        for (Permisos ev : eventos) {
            String color = switch (ev.getEstado()) {
                case "ACEPTADO" -> "green";
                case "PROCESANDO" -> "#007BFF";
                case "RECHAZADO" -> "red";
                default -> "gray";
            };

            Span etiqueta = new Span(ev.getMotivo());
            etiqueta.getStyle().set("background-color", color).set("color", "white").set("font-size", "0.75em").set("padding", "2px 4px").set("border-radius", "3px").set("display", "block").set("margin-top", "4px");
            dia.add(etiqueta);
        }

        dia.addClickListener(e -> {
            Dialog dialogo = new Dialog();
            dialogo.setWidth("500px");
            
            dialogo.setCloseOnEsc(true);
            dialogo.setCloseOnOutsideClick(true);
            
            Span titulo = new Span("Detalles del día " + fechaFormateada);
            titulo.getStyle().set("font-weight", "bold").set("font-size", "18px");
            
            Span titulo2 = new Span("Permiso día " + fechaFormateada);
            titulo2.getStyle().set("font-weight", "bold").set("font-size", "18px");
            titulo2.setVisible(false);

            VerticalLayout contenidoDialogo1 = new VerticalLayout();
            contenidoDialogo1.setSpacing(true);
            contenidoDialogo1.setPadding(true);

            VerticalLayout contenidoDialogo2 = new VerticalLayout();
            contenidoDialogo2.setSpacing(false);
            contenidoDialogo2.setPadding(true);
            contenidoDialogo2.setVisible(false); 
            contenidoDialogo2.getStyle().set("gap", "0"); 

            if (eventos.isEmpty()) {
                Span sinEventos = new Span("No hay eventos en este día.");
                contenidoDialogo1.add(sinEventos);
            } else {
            	for (Permisos ev : eventos) {
                    Span permiso = new Span(ev.getMotivo() + " (" + ev.getEstado() + ")");
                    permiso.getStyle().set("flex-grow", "1");

                    Button anularBtn = new Button("Anular");
                    anularBtn.getStyle().set("background-color", "red").set("color", "white").set("font-size", "16px").set("cursor", "pointer");

                    anularBtn.addClickListener(click -> {
                        permisosRepositorio.delete(ev); 
                        Notification.show("Permiso anulado", 2000, Notification.Position.TOP_CENTER);
                        dialogo.close(); 
                        crearContenido(); 
                    });

                    HorizontalLayout permisoLayout = new HorizontalLayout(permiso, anularBtn);
                    permisoLayout.setWidthFull();
                    permisoLayout.setAlignItems(Alignment.CENTER);
                    permisoLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
                    contenidoDialogo1.add(permisoLayout);
                }
            }

            Select<String> permisoTipo = new Select<>();
            permisoTipo.setLabel("Tipo de permiso");
            permisoTipo.setItems("Vacaciones", "Asuntos propios", "Médico", "Otros motivos");
            permisoTipo.setPlaceholder("Selecciona un permiso");
            
            DatePicker fechaInicio = new DatePicker("Desde");
            fechaInicio.setValue(fecha);
            DatePicker fechaFin = new DatePicker("Hasta");
            fechaInicio.setVisible(false);
            fechaFin.setVisible(false);
            
            HorizontalLayout fechasLayout = new HorizontalLayout(fechaInicio, fechaFin);
            
            permisoTipo.addValueChangeListener(event -> {
                String tipo = event.getValue();
                boolean esVacaciones = "Vacaciones".equals(tipo);
                fechaInicio.setVisible(esVacaciones);
                fechaFin.setVisible(esVacaciones);
            });
            
            TextField comentarioField = new TextField("Observaciones");
            comentarioField.setMaxLength(144);
            comentarioField.setPlaceholder("Introduce un comentario (máx. 144 caracteres)");
            comentarioField.setWidth("200px");
            
            contenidoDialogo2.add(permisoTipo, fechasLayout, comentarioField);
            
            Button solicitar = new Button("Solicitar permiso");
            solicitar.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer");
            
            Button solicitar2 = new Button("Solicitar permiso");
            solicitar2.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer");
            solicitar2.setVisible(false);  

            solicitar.addClickListener(event -> {
                titulo.setVisible(false);
                titulo2.setVisible(true);
                contenidoDialogo1.setVisible(false); 
                contenidoDialogo2.setVisible(true); 
                solicitar.setVisible(false);
                solicitar2.setVisible(true);
            });
            
            solicitar2.addClickListener(e2 -> {
                String motivo = permisoTipo.getValue();
                String observaciones = comentarioField.getValue() != null ? comentarioField.getValue() : null;
                LocalDate fechaFinValor = fechaFin.getValue() != null ? fechaFin.getValue() : null;
                registroPermiso(fecha, fechaFinValor, motivo, observaciones);
                Notification.show("Permiso solicitado correctamente", 2000, Notification.Position.TOP_CENTER);
                dialogo.close();
           });

            HorizontalLayout botones = new HorizontalLayout(solicitar, solicitar2);

            dialogo.add(titulo, titulo2, contenidoDialogo1, contenidoDialogo2, botones);
            dialogo.open();

        });
        return dia;
    }

    private void cambiarMes(int indx) {
        currentMonth = currentMonth.plusMonths(indx);
        contenido.removeAll(); 
        contenido.add(crearCabecera(currentMonth)); 
        contenido.add(crearCalendario(currentMonth)); 
    }

    private List<Permisos> obtenerPermisos() {
        return permisosRepositorio.findBySolicitanteId(usuarioActual.getId());
    }
    
    private void registroPermiso(LocalDate fecha, LocalDate fechaAux, String motivo, String observaciones) {
        Permisos registroPermiso = new Permisos();
        Usuario Supervisor = usuarioRepositorio.findByEmpresaIdAndRolId(usuarioActual.getEmpresa().getId(), 2);
        registroPermiso.setSolicitante(usuarioActual);
        registroPermiso.setSolicitado(Supervisor);
        registroPermiso.setTipo("PERMISO");
        registroPermiso.setEstado("PROCESANDO");
        registroPermiso.setFecha(fecha);
        registroPermiso.setFechaAux(fechaAux);
        registroPermiso.setMotivo(motivo);
        registroPermiso.setObservaciones(observaciones);
           
        permisosRepositorio.save(registroPermiso);
        registroNotificacion(registroPermiso);
        emailNotificacion.enviarCorreoPermiso(Supervisor.getEmpresa().getId(), Supervisor.getEmail(), usuarioActual.getNombre(), motivo, fecha, fechaAux); 
        crearContenido();
    }
    
    private void registroNotificacion(Permisos permiso) {
        Notificaciones registroNotificaciones = new Notificaciones();
        Usuario Supervisor = usuarioRepositorio.findByEmpresaIdAndRolId(usuarioActual.getEmpresa().getId(), 2);
        registroNotificaciones.setSolicitante(usuarioActual);
        registroNotificaciones.setSolicitado(Supervisor);
        registroNotificaciones.setidAsociado(permiso.getId());
        registroNotificaciones.setEstado(0);
        registroNotificaciones.setTipo("PERMISO");
           
        notificacionesRepositorio.save(registroNotificaciones);
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

        List<Notificaciones> pendientes = notificacionesRepositorio.findByEstadoAndSolicitanteIdAndResolucionNotNull(1, usuarioActual.getId());

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