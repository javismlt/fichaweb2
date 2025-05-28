package com.gts.fichaweb.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import java.time.format.DateTimeFormatter;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.applayout.AppLayout;
import modelos.Usuario;
import modelos.Registro;
import modelos.Solicitudes;
import modelos.Notificaciones;
import repositorios.NotificacionesRepositorio;
import repositorios.RegistroRepositorio;
import repositorios.UsuarioRepositorio;
import servicios.EmailServicio;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import java.time.LocalDate;
import com.vaadin.flow.component.grid.Grid;
import java.util.List;
import java.util.Optional;
import modelos.Notificaciones;
import modelos.Solicitudes;
import modelos.Permisos;
import repositorios.NotificacionesRepositorio;
import repositorios.SolicitudesRepositorio;
import repositorios.PermisosRepositorio;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.vaadin.flow.component.notification.Notification;
import java.io.ByteArrayOutputStream;
import com.itextpdf.layout.element.Paragraph;
import java.util.stream.Collectors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import java.io.IOException;
import java.time.LocalTime;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.dialog.Dialog;
import java.time.Duration;
import org.apache.poi.ss.util.CellRangeAddress;
import java.util.Comparator;
import com.vaadin.flow.component.dependency.CssImport;
import modelos.Logs_modificaciones;
import modelos.Notificaciones;
import modelos.Permisos;
import repositorios.Logs_modificacionesRepositorio;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import repositorios.SolicitudesRepositorio;
import servicios.EmailNotificacion;

@Route("consulta")
@CssImport(value = "./themes/my-theme/styles.css", themeFor = "vaadin-grid") 
public class ConsultaVista extends AppLayout {
    private final UsuarioRepositorio usuarioRepositorio;
    private final RegistroRepositorio registroRepositorio; 
    private final SolicitudesRepositorio solicitudesRepositorio; 
    private final NotificacionesRepositorio notificacionesRepositorio; 
    private final PermisosRepositorio permisosRepositorio; 
    private Usuario usuarioActual;
    private Usuario usuarioActualAux;
    private Grid<Registro> grid;
    private Span totalHorasTrabajadasLabel;
    private DatePicker fechaInicio;
    private DatePicker fechaFin;
    private String nombreUsuario;
    private final EmailNotificacion emailNotificacion;
    
    public ConsultaVista(UsuarioRepositorio usuarioRepositorio, RegistroRepositorio registroRepositorio, SolicitudesRepositorio solicitudesRepositorio, NotificacionesRepositorio notificacionesRepositorio, PermisosRepositorio permisosRepositorio, EmailNotificacion emailNotificacion) {
        this.registroRepositorio = registroRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.solicitudesRepositorio = solicitudesRepositorio; 
        this.notificacionesRepositorio = notificacionesRepositorio;
        this.permisosRepositorio = permisosRepositorio;
        this.emailNotificacion = emailNotificacion;
        
        nombreUsuario = (String) VaadinSession.getCurrent().getAttribute("username");
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
        
        Integer solicitanteId = (usuarioActualAux != null) ? usuarioActualAux.getId() : usuarioActual.getId();
        int notificacionesPendientes = notificacionesRepositorio.countByEstadoAndSolicitanteIdAndResolucionNotNull(1, solicitanteId);
        
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
        fechaInicio = new DatePicker("Fecha inicio");
        fechaFin = new DatePicker("Fecha fin");
        fechaFin.setValue(LocalDate.now());

        fechaInicio.setLocale(new java.util.Locale("es", "ES"));
        fechaFin.setLocale(new java.util.Locale("es", "ES"));
        
        HorizontalLayout fechasLayout = new HorizontalLayout();
        fechasLayout.setAlignItems(Alignment.CENTER);
        fechasLayout.addClassName("fechas-layout");
        fechasLayout.setWidthFull();
        
        grid = new Grid<>(Registro.class); 
        grid.setHeightFull();
        grid.removeAllColumns();
        grid.addClassName("responsive-grid");
        
        grid.addColumn(registro -> registro.getFechaRegistro().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).setHeader("Fecha").setAutoWidth(true);
        grid.addColumn(registro -> registro.getAccion()).setHeader("Acción").setAutoWidth(true);
        grid.addColumn(registro -> {LocalTime hora = registro.getHora();return hora != null ? hora.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "";}).setHeader("Hora");
        grid.addColumn(new ComponentRenderer<>(registro -> {
            Span span = new Span();
            if (Integer.valueOf(1).equals(registro.getValidado())) {
                span.add("🟢");
            } else if (Integer.valueOf(0).equals(registro.getValidado())) {
            	span.add("🔴");
            }
            return span;
        })).setHeader("Validado").setAutoWidth(true);
        grid.addColumn(Registro::getObservaciones).setHeader("Observaciones");
        
        grid.addSelectionListener(event -> {
            event.getFirstSelectedItem().ifPresent(registro -> {
                Dialog dialog = new Dialog();
                dialog.setWidth("500px");

                dialog.setCloseOnEsc(true);
                dialog.setCloseOnOutsideClick(true);

                Span titulo = new Span("Detalles del Registro");
                titulo.getStyle().set("font-weight", "bold").set("font-size", "18px");

                VerticalLayout contenido = new VerticalLayout();
                
                Span fechaSpan = new Span("Fecha: " + registro.getFechaRegistro());
                Span accionSpan = new Span("Acción: " + registro.getAccion());
                Span observacionesSpan = new Span("Observaciones: " + registro.getObservaciones());

                LocalTime hora = registro.getHora();
                String horaFormateada = hora != null ? hora.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "N/A";
                
                Span horaSpan = new Span("Hora: " + horaFormateada);
                TextField horaTextField = new TextField("Hora");
                horaTextField.setValue(horaFormateada);
                horaTextField.setVisible(false);  
                horaTextField.setWidthFull();
                
                contenido.add(fechaSpan, accionSpan, horaSpan, horaTextField, observacionesSpan);

                Button actualizar = new Button("Solicitar cambio");
                actualizar.setVisible(false);  
                actualizar.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer");

                Button modificar = new Button("Modificar");
                modificar.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer");

                modificar.addClickListener(e -> {
                    horaSpan.setVisible(false);
                    horaTextField.setVisible(true);
                    modificar.setVisible(false);
                    actualizar.setVisible(true);
                });

                actualizar.addClickListener(e -> {
                     LocalTime nuevaHora = LocalTime.parse(horaTextField.getValue(), DateTimeFormatter.ofPattern("HH:mm:ss"));
                     String previaHoraStr = registro.getHora().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                     String nuevaHoraStr = (nuevaHora != null) ? nuevaHora.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "N/A";
                     String nombreCampo = "hora";
                     
                     boolean solicitado = registroSolicitud(registro, nombreCampo, previaHoraStr, nuevaHoraStr, registro.getAccion());
                     if (solicitado) {
                    	 registro.setValidado(0);
                         registroRepositorio.save(registro);
                     }
                     
                     Notification.show("Modificación solicitada correctamente", 2000, Notification.Position.TOP_CENTER);
                     actualizarGrid(fechaInicio.getValue(), fechaFin.getValue());
                     dialog.close();
                });
                HorizontalLayout botones = new HorizontalLayout(modificar, actualizar);
                dialog.add(titulo, contenido, botones);
                dialog.open();
            });
        });
        
        if (usuarioActual.getRol() == 3) {
        	Select<Usuario> selectUsuarios = new Select<>();
            selectUsuarios.setLabel("Usuarios");
            
            List<Usuario> usuariosDeLaEmpresa = usuarioRepositorio.findByEmpresaId(usuarioActual.getEmpresa().getId());
            usuariosDeLaEmpresa = usuariosDeLaEmpresa.stream().filter(usuario -> usuario.getRol() == 4 && usuario.getActivo() == 1).collect(Collectors.toList());

            selectUsuarios.setItems(usuariosDeLaEmpresa);
            selectUsuarios.setItemLabelGenerator(Usuario::getLoginUsuario);
            selectUsuarios.setPlaceholder("Seleccione un usuario");
            fechasLayout.add(selectUsuarios);
            Dialog pinDialog = new Dialog();

            HorizontalLayout fila0 = new HorizontalLayout();
            TextField pinField = new TextField("");
            pinField.setPlaceholder("PIN");
            pinField.setMaxLength(4); 
            pinField.setWidth("100%");
            fila0.add(pinField);

            HorizontalLayout fila1 = new HorizontalLayout();
            fila1.setSpacing(true);
            Button botonNum1 = new Button("1");
            Button botonNum2 = new Button("2");
            Button botonNum3 = new Button("3");
            botonNum1.getStyle().set("cursor", "pointer");
            botonNum2.getStyle().set("cursor", "pointer");
            botonNum3.getStyle().set("cursor", "pointer");
            fila1.add(botonNum1, botonNum2, botonNum3);

            HorizontalLayout fila2 = new HorizontalLayout();
            fila2.setSpacing(true);
            Button botonNum4 = new Button("4");
            Button botonNum5 = new Button("5");
            Button botonNum6 = new Button("6");
            botonNum4.getStyle().set("cursor", "pointer");
            botonNum5.getStyle().set("cursor", "pointer");
            botonNum6.getStyle().set("cursor", "pointer");
            fila2.add(botonNum4, botonNum5, botonNum6);

            HorizontalLayout fila3 = new HorizontalLayout();
            fila3.setSpacing(true);
            Button botonNum7 = new Button("7");
            Button botonNum8 = new Button("8");
            Button botonNum9 = new Button("9");
            botonNum7.getStyle().set("cursor", "pointer");
            botonNum8.getStyle().set("cursor", "pointer");
            botonNum9.getStyle().set("cursor", "pointer");
            fila3.add(botonNum7, botonNum8, botonNum9);

            HorizontalLayout fila4 = new HorizontalLayout();
            fila4.setSpacing(true);
            Button botonLimpiar = new Button("CLEAR");
            botonLimpiar.getStyle().set("background-color", "red").set("color", "white").set("cursor", "pointer");
            Button botonNum0 = new Button("0");
            botonNum0.getStyle().set("cursor", "pointer");
            Button verificarPinButton = new Button("OK");
            verificarPinButton.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer");
            fila4.add(botonLimpiar, botonNum0, verificarPinButton);

            botonNum1.addClickListener(click -> agregarNumero(pinField, "1"));
            botonNum2.addClickListener(click -> agregarNumero(pinField, "2"));
            botonNum3.addClickListener(click -> agregarNumero(pinField, "3"));
            botonNum4.addClickListener(click -> agregarNumero(pinField, "4"));
            botonNum5.addClickListener(click -> agregarNumero(pinField, "5"));
            botonNum6.addClickListener(click -> agregarNumero(pinField, "6"));
            botonNum7.addClickListener(click -> agregarNumero(pinField, "7"));
            botonNum8.addClickListener(click -> agregarNumero(pinField, "8"));
            botonNum9.addClickListener(click -> agregarNumero(pinField, "9"));
            botonNum0.addClickListener(click -> agregarNumero(pinField, "0"));
            botonLimpiar.addClickListener(click -> pinField.clear());

            verificarPinButton.addClickListener(click -> {
                Usuario usuarioSeleccionado = selectUsuarios.getValue();
                if (usuarioSeleccionado != null) {
                    String pinIngresado = pinField.getValue();
                    if (usuarioSeleccionado.getPin().equals(pinIngresado)) {
                        pinDialog.close();
                        Notification.show("PIN correcto, acceso concedido", 2000, Notification.Position.TOP_CENTER);
                        usuarioActualAux = usuarioSeleccionado;
                        getElement().removeAllChildren(); 
                        crearHeader(nombreUsuario);
                        crearContenido();
                        actualizarGrid(fechaInicio.getValue(), fechaFin.getValue());
                    } else {
                        Notification.show("PIN incorrecto inténtalo de nuevo", 2000, Notification.Position.TOP_CENTER);
                        pinField.clear();
                    }
                }
            });

            pinDialog.add(fila0, fila1, fila2, fila3, fila4);

            selectUsuarios.addValueChangeListener(event -> {
                Usuario usuarioSeleccionado = event.getValue();
                if (usuarioSeleccionado != null) {
                    System.out.println("Usuario seleccionado: " + usuarioSeleccionado.getNombre());
                    pinField.setValue("");
                    pinDialog.open();
                }
            });
        }
        
        fechasLayout.add(fechaInicio, fechaFin);

        fechaInicio.addValueChangeListener(event -> actualizarGrid(fechaInicio.getValue(), fechaFin.getValue()));
        fechaFin.addValueChangeListener(event -> actualizarGrid(fechaInicio.getValue(), fechaFin.getValue()));
        
        totalHorasTrabajadasLabel = new Span("");
        totalHorasTrabajadasLabel.getStyle().set("font-weight", "bold").set("font-size", "16px");
        totalHorasTrabajadasLabel.addClassName("horas-trabajadas-label");
        
        Button descargarPdf = new Button("Descargar PDF", e -> generarPdf(grid));
        Button descargarExcel = new Button("Descargar EXCEL", e -> generarExcel(grid));

        HorizontalLayout botonesLayout = new HorizontalLayout(descargarPdf, descargarExcel);
        botonesLayout.setAlignItems(Alignment.CENTER);
        botonesLayout.addClassName("botones-layout");
        botonesLayout.setWidthFull();
        
        descargarPdf.getStyle().set("margin-top", "37px").set("color", "white").set("background-color", "red").set("cursor", "pointer");
        descargarExcel.getStyle().set("margin-top", "37px").set("color", "white").set("background-color", "green").set("cursor", "pointer");

        fechasLayout.add(botonesLayout);

        VerticalLayout contenidoPrincipal = new VerticalLayout(fechasLayout, totalHorasTrabajadasLabel, grid); 
        contenidoPrincipal.setPadding(true);
        contenidoPrincipal.setAlignItems(Alignment.START);
        contenidoPrincipal.setWidthFull();
        contenidoPrincipal.setHeightFull();
        
        setContent(contenidoPrincipal);
        actualizarGrid(fechaInicio.getValue(), fechaFin.getValue());
    }


    private void actualizarGrid(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Registro> registros;
        Integer usuarioId = (usuarioActualAux != null) ? usuarioActualAux.getId() : usuarioActual.getId();
        if (fechaInicio != null && fechaFin != null) {
        	registros = registroRepositorio.findByFechaRegistroBetweenAndUsuario_IdAndActivo(fechaInicio, fechaFin, usuarioId.intValue(),1);
        } else {
        	LocalDate hoy = LocalDate.now();
            registros = registroRepositorio.findByFechaRegistroAndUsuario_IdAndActivo(hoy, usuarioId.intValue(), 1);
        }
        registros.sort(
        	    Comparator.comparing(Registro::getFechaRegistro, Comparator.reverseOrder())
        	              .thenComparing(Registro::getHora, Comparator.reverseOrder())
        );
        grid.setItems(registros);
        
        String horasTrabajadas = calcularHorasTrabajadas(registros);
        totalHorasTrabajadasLabel.setText("TRABAJADO: " + horasTrabajadas + " horas");
    }
    
    private String calcularHorasTrabajadas(List<Registro> registros) {
        registros.sort((r1, r2) -> {
            int cmpFecha = r1.getFechaRegistro().compareTo(r2.getFechaRegistro());
            if (cmpFecha != 0) return cmpFecha;
            return r1.getHora().compareTo(r2.getHora());
        });

        Duration totalTrabajado = Duration.ZERO;
        Duration totalDescanso = Duration.ZERO;

        LocalTime inicioTrabajado = null;
        LocalTime inicioDescanso = null;
        
        String accionAnterior = null;

        for (Registro reg : registros) {
            String accion = reg.getAccion().toLowerCase();
            LocalTime hora = reg.getHora();

            if (hora == null) continue;

            switch (accion) {
	            case "entrada":
	                if ("entrada".equals(accionAnterior)) {
	                    inicioTrabajado = null;
	                } else if ("retorno".equals(accionAnterior)) {
	                    inicioTrabajado = null;
	                    inicioTrabajado = hora;
	                } else if ("pausa".equals(accionAnterior)) {
	                    inicioDescanso = null; 
	                    inicioTrabajado = hora;
	                } else {
	                    if (inicioTrabajado != null) {
	                        Duration trabajado = Duration.between(inicioTrabajado, hora);
	                        totalTrabajado = totalTrabajado.plus(trabajado);
	                    }
	                    inicioTrabajado = hora;
	                }
	                break;

                case "pausa":
                    if (inicioTrabajado != null) {
                        Duration trabajado = Duration.between(inicioTrabajado, hora);
                        totalTrabajado = totalTrabajado.plus(trabajado);
                        inicioTrabajado = null;
                    }
                    inicioDescanso = hora;
                    break;

                case "retorno":
                    if (inicioDescanso != null) {
                        Duration descanso = Duration.between(inicioDescanso, hora);
                        totalDescanso = totalDescanso.plus(descanso);
                        inicioDescanso = null;
                    }
                    if (inicioTrabajado == null) {
                        inicioTrabajado = hora;
                    }
                    break;

                case "salida":
                    if (inicioTrabajado != null) {
                        Duration trabajado = Duration.between(inicioTrabajado, hora);
                        totalTrabajado = totalTrabajado.plus(trabajado);
                        inicioTrabajado = null;
                    }
                    if (inicioDescanso != null) {
                        Duration descanso = Duration.between(inicioDescanso, hora);
                        totalDescanso = totalDescanso.plus(descanso);
                        inicioDescanso = null;
                    }
                    break;
            }
            accionAnterior = accion;
        }

        if (inicioTrabajado != null || inicioDescanso != null) {
            Registro ultimo = registros.get(registros.size() - 1);
            LocalTime ultimaHora = ultimo.getHora();

            if (inicioTrabajado != null) {
                Duration trabajado = Duration.between(inicioTrabajado, ultimaHora);
                totalTrabajado = totalTrabajado.plus(trabajado);
            }

            if (inicioDescanso != null) {
                Duration descanso = Duration.between(inicioDescanso, ultimaHora);
                totalDescanso = totalDescanso.plus(descanso);
            }
        }

        registros.sort((r1, r2) -> {
            int cmpFecha = r2.getFechaRegistro().compareTo(r1.getFechaRegistro());
            if (cmpFecha != 0) return cmpFecha;
            return r2.getHora().compareTo(r1.getHora());
        });

        long horas = totalTrabajado.toHours();
        long minutos = totalTrabajado.toMinutes() % 60;
        return String.format("%02d:%02d", horas, minutos);
    }

    private void generarPdf(Grid<Registro> grid) {
        List<Registro> registros = grid.getListDataView().getItems().collect(Collectors.toList());

        if (registros.isEmpty()) {
            Notification.show("No hay registros para generar el PDF", 2000, Notification.Position.TOP_CENTER);
            return;
        }
        
        String horasTrabajadas = calcularHorasTrabajadas(registros);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        Paragraph titulo = new Paragraph("FichaWeb").setFontSize(28) .setBold(); 
        document.add(titulo);
        
        Paragraph titulo2 = new Paragraph("Registros de Jornada Laboral").setFontSize(14);
        document.add(titulo2);

        Usuario usuario = (usuarioActualAux != null) ? usuarioActualAux : usuarioActual;
        Paragraph usuarioParrafo = new Paragraph("Usuario: " + usuario.getNombre() + " (" + usuario.getEmpresa().getNombreComercial() + ")").setFontSize(12);
        document.add(usuarioParrafo);

        LocalDate fechaInicioValor = fechaInicio.getValue() != null ? fechaInicio.getValue() : LocalDate.now();
        LocalDate fechaFinValor = fechaFin.getValue() != null ? fechaFin.getValue() : LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaInicioFormateada = fechaInicioValor.format(formatter);
        String fechaFinFormateada = fechaFinValor.format(formatter);

        String rangoFechas;
        if (fechaInicioValor.equals(fechaFinValor)) {
            rangoFechas = "Fecha: " + fechaInicioFormateada;
        } else {
            rangoFechas = "Fecha: " + fechaInicioFormateada + " - " + fechaFinFormateada;
        }

        Paragraph fechasParrafo = new Paragraph(rangoFechas).setFontSize(12);
        document.add(fechasParrafo);

        Paragraph horasParrafo = new Paragraph("Total trabajado: " + horasTrabajadas + " horas").setFontSize(12).setMarginBottom(15);
        document.add(horasParrafo);
        
        Table table = new Table(6);
        table.addHeaderCell(new Cell().add(new Paragraph("FECHA")));
        table.addHeaderCell(new Cell().add(new Paragraph("ACCION")));
        table.addHeaderCell(new Cell().add(new Paragraph("HORA")));
        table.addHeaderCell(new Cell().add(new Paragraph("ORIGEN")));
        table.addHeaderCell(new Cell().add(new Paragraph("ESTADO")));
        table.addHeaderCell(new Cell().add(new Paragraph("OBSERVACIONES")));

        for (Registro registro : registros) {
        	table.addCell(new Cell().add(new Paragraph(registro.getFechaRegistro().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))));
            table.addCell(new Cell().add(new Paragraph(registro.getAccion())));
            table.addCell(new Cell().add(new Paragraph(registro.getHora() != null ? registro.getHora().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "")));
            table.addCell(new Cell().add(new Paragraph(registro.getOrigen())));
            table.addCell(new Cell().add(new Paragraph(registro.getValidado() == 1 ? "VALIDADO" : "NO VALIDADO")));
            table.addCell(new Cell().add(new Paragraph(registro.getObservaciones())));
        }

        document.add(table);
        document.close();

        byte[] pdfBytes = byteArrayOutputStream.toByteArray();
        String base64Pdf = java.util.Base64.getEncoder().encodeToString(pdfBytes);

        String nombreArchivo;
        if (fechaInicioValor.equals(fechaFinValor)) {
            nombreArchivo = "FICHAJES_" + usuarioActual.getNombre() + "_" + fechaInicioFormateada + ".pdf";
        } else {
            nombreArchivo = "FICHAJES_" + usuarioActual.getNombre() + "_" + fechaInicioFormateada + "_" + fechaFinFormateada + ".pdf";
        }

        UI.getCurrent().getPage().executeJs(
            "var link = document.createElement('a');" +
            "link.href = 'data:application/pdf;base64,' + $0;" +
            "link.download = '" + nombreArchivo + "';" +
            "link.click();", base64Pdf);
    }

    private void generarExcel(Grid<Registro> grid) {
        List<Registro> registros = grid.getListDataView().getItems().collect(Collectors.toList());

        if (registros.isEmpty()) {
        	Notification.show("No hay registros para generar el EXCEL", 2000, Notification.Position.TOP_CENTER);
            return;
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Registros");

        XSSFRow titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("FichaWeb");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        XSSFRow userRow = sheet.createRow(1);
        userRow.createCell(0).setCellValue("Usuario: " + usuarioActual.getNombre() + " (" + usuarioActual.getEmpresa().getNombreComercial() + ")");
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

        LocalDate fechaInicioValor = fechaInicio.getValue() != null ? fechaInicio.getValue() : LocalDate.now();
        LocalDate fechaFinValor = fechaFin.getValue() != null ? fechaFin.getValue() : LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaInicioFormateada = fechaInicioValor.format(formatter);
        String fechaFinFormateada = fechaFinValor.format(formatter);

        String rangoFechas;
        if (fechaInicioValor.equals(fechaFinValor)) {
            rangoFechas = "Fecha: " + fechaInicioFormateada;
        } else {
            rangoFechas = "Fecha: " + fechaInicioFormateada + " - " + fechaFinFormateada;
        }

        XSSFRow dateRow = sheet.createRow(2);
        dateRow.createCell(0).setCellValue(rangoFechas);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 4));

        String horasTrabajadas = calcularHorasTrabajadas(registros);
        XSSFRow hoursRow = sheet.createRow(3);
        hoursRow.createCell(0).setCellValue("Total trabajado: " + horasTrabajadas + " horas");
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 4));

        XSSFRow headerRow = sheet.createRow(5);
        headerRow.createCell(0).setCellValue("FECHA");
        headerRow.createCell(1).setCellValue("ACCION");
        headerRow.createCell(2).setCellValue("HORA");
        headerRow.createCell(3).setCellValue("ORIGEN");
        headerRow.createCell(4).setCellValue("ESTADO");
        headerRow.createCell(5).setCellValue("OBSERVACIONES");

        int rowIndex = 6; 
        for (Registro registro : registros) {
            XSSFRow row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(registro.getFechaRegistro().format(formatter));
            row.createCell(1).setCellValue(registro.getAccion());
            row.createCell(2).setCellValue(registro.getHora() != null ? registro.getHora().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "");
            row.createCell(3).setCellValue(registro.getOrigen());
            row.createCell(4).setCellValue(registro.getValidado() == 1 ? "VALIDADO" : "NO VALIDADO");
            row.createCell(5).setCellValue(registro.getObservaciones());
        }

        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }

        String nombreArchivo;
        if (fechaInicioValor.equals(fechaFinValor)) {
            nombreArchivo = "FICHAJES_" + usuarioActual.getNombre() + "_" + fechaInicioFormateada + ".xlsx";
        } else {
            nombreArchivo = "FICHAJES_" + usuarioActual.getNombre() + "_" + fechaInicioFormateada + "_" + fechaFinFormateada + ".xlsx";
        }
        
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            workbook.write(byteArrayOutputStream);
            byte[] excelBytes = byteArrayOutputStream.toByteArray();

            String base64Excel = java.util.Base64.getEncoder().encodeToString(excelBytes);
            UI.getCurrent().getPage().executeJs(
                "var link = document.createElement('a');" +
                "link.href = 'data:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;base64,' + $0;" +
                "link.download = '" + nombreArchivo + "';" +
                "link.click();", base64Excel);
        } catch (IOException e) {
            Notification.show("Error al generar el archivo Excel");
        }
    }
    
    private void agregarNumero(TextField pinField, String numero) {
        String total = pinField.getValue();
        if (total.length() < 4) {
            pinField.setValue(total + numero);
        }
    }
    
    private boolean registroSolicitud(Registro registro, String campo, String valorPrevio, String valorNuevo, String accion) {
        Solicitudes registroSolicitud = new Solicitudes();
        Usuario Supervisor = usuarioRepositorio.findByEmpresaIdAndRolId(usuarioActual.getEmpresa().getId(), 2);
        Usuario usuario = (usuarioActualAux != null) ? usuarioActualAux : usuarioActual;
        registroSolicitud.setRegistro(registro);
        registroSolicitud.setSolicitante(usuario);
        registroSolicitud.setSolicitado(Supervisor);
        registroSolicitud.setTipo("UPDATE");
        registroSolicitud.setCampo(campo);
        registroSolicitud.setValorPrevio(valorPrevio);
        registroSolicitud.setValor(valorNuevo);
        registroSolicitud.setAccion(accion);
        registroSolicitud.setEstado("PROCESANDO");
        
        solicitudesRepositorio.save(registroSolicitud);
        registroNotificacion(registroSolicitud);
        emailNotificacion.enviarCorreoSolicitud(Supervisor.getEmail(), usuario.getNombre(), "modificacion", accion, valorNuevo, registro.getHora(), registro.getFechaRegistro()); 
        actualizarGrid(fechaInicio.getValue(), fechaFin.getValue());
        return true;
    }
    
    private void registroNotificacion(Solicitudes solicitud) {
        Notificaciones registroNotificaciones = new Notificaciones();
        Usuario Supervisor = usuarioRepositorio.findByEmpresaIdAndRolId(usuarioActual.getEmpresa().getId(), 2);
        Usuario usuario = (usuarioActualAux != null) ? usuarioActualAux : usuarioActual;
        registroNotificaciones.setSolicitante(usuario);
        registroNotificaciones.setSolicitado(Supervisor);
        registroNotificaciones.setidAsociado(solicitud.getId());
        registroNotificaciones.setEstado(0);
        registroNotificaciones.setTipo("UPDATE");

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

        Integer solicitanteId = (usuarioActualAux != null) ? usuarioActualAux.getId() : usuarioActual.getId();
        List<Notificaciones> pendientes = notificacionesRepositorio.findByEstadoAndSolicitanteIdAndResolucionNotNull(1, solicitanteId);

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