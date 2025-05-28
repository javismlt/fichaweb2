package com.gts.fichaweb.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
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
import repositorios.RegistroRepositorio;
import repositorios.UsuarioRepositorio;
import servicios.EmailNotificacion;

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
import java.util.ArrayList;
import java.util.Collections;

@Route("modregistros")
@CssImport(value = "./themes/my-theme/styles.css", themeFor = "vaadin-grid") 
public class ModRegistro extends AppLayout {
    private final UsuarioRepositorio usuarioRepositorio;
    private final RegistroRepositorio registroRepositorio;
    private final Logs_modificacionesRepositorio logs_modificacionesRepositorio;  
    private Usuario usuarioActual;
    private Usuario usuarioActualAux;
    private Grid<Registro> grid;
    private Span totalHorasTrabajadasLabel;
    private DatePicker fechaInicio;
    private DatePicker fechaFin;
    private VerticalLayout contenidoPrincipal;
    private final NotificacionesRepositorio notificacionesRepositorio; 
    private final PermisosRepositorio permisosRepositorio; 
    private final SolicitudesRepositorio solicitudesRepositorio; 
    private final EmailNotificacion emailNotificacion;

    public ModRegistro(UsuarioRepositorio usuarioRepositorio, RegistroRepositorio registroRepositorio, Logs_modificacionesRepositorio logs_modificacionesRepositorio, NotificacionesRepositorio notificacionesRepositorio, PermisosRepositorio permisosRepositorio, SolicitudesRepositorio solicitudesRepositorio, EmailNotificacion emailNotificacion) {
        this.registroRepositorio = registroRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.logs_modificacionesRepositorio = logs_modificacionesRepositorio; 
        this.notificacionesRepositorio = notificacionesRepositorio;
        this.permisosRepositorio = permisosRepositorio;
        this.solicitudesRepositorio = solicitudesRepositorio;
        this.emailNotificacion = emailNotificacion;
        
        String nombreUsuario = (String) VaadinSession.getCurrent().getAttribute("username");
        if (nombreUsuario == null) {
            getElement().executeJs("window.location.href='/'");
        } else {
            this.usuarioActual = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
            if (usuarioActual.getRol() == 1 || usuarioActual.getRol() == 2) {
            	crearHeader(nombreUsuario);
                crearContenido();
            } else {
            	Notification.show("Usuario no administrador", 2000, Notification.Position.TOP_CENTER);
                getElement().executeJs("setTimeout(() => window.location.href='/', 2000)");
                return;
            }
        }
    }

    private void crearHeader(String nombreUsuario) {
        Anchor enlaceEmpresas = new Anchor("usuario", "Empresas");
        enlaceEmpresas.getElement().setAttribute("href", "/listempresas");
        enlaceEmpresas.getStyle().set("color", "black").set("text-decoration", "none").set("font-size", "16px");
        
        Anchor enlaceUsuarios = new Anchor("usuario", "Usuarios");
        enlaceUsuarios.getElement().setAttribute("href", "/listusuarios");
        enlaceUsuarios.getStyle().set("color", "black").set("text-decoration", "none").set("font-size", "16px");
        
        Anchor enlaceRegistros = new Anchor("registro", "Registros");
        enlaceRegistros.getElement().setAttribute("href", "/modregistros");
        enlaceRegistros.getStyle().set("color", "black").set("text-decoration", "none").set("font-size", "16px");

        HorizontalLayout menuIzquierdo = new HorizontalLayout();
        menuIzquierdo.add(enlaceEmpresas, enlaceUsuarios, enlaceRegistros);
        menuIzquierdo.setSpacing(true);
        menuIzquierdo.getStyle().set("gap", "25px");
        menuIzquierdo.setAlignItems(Alignment.CENTER);

        Button menuDesplegable = new Button("☰"); 
        menuDesplegable.getStyle().set("font-size", "24px").set("background", "none").set("border", "1px solid black").set("cursor", "pointer").set("border-radius", "4px").set("display", "none");

        ContextMenu menuResponsive = new ContextMenu(menuDesplegable);
        menuResponsive.setOpenOnClick(true);
        menuResponsive.addItem("Empresas", e -> UI.getCurrent().navigate("listempresas"));
        menuResponsive.addItem("Usuarios", e -> UI.getCurrent().navigate("listusuarios"));
        menuResponsive.addItem("Registros", e -> UI.getCurrent().navigate("modregistros"));
        
        menuIzquierdo.getElement().getClassList().add("menu-izquierdo");
        menuDesplegable.getElement().getClassList().add("menu-desplegable");
        
        int notificacionesPendientes = notificacionesRepositorio.countByEstado(0);
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

        Select<Usuario> selectUsuarios = new Select<>();
        selectUsuarios.setLabel("Usuarios");
        
        Select<Integer> selectValidado = new Select<>();
        selectValidado.setLabel("Estado");
        selectValidado.setItems(1, 0);
        selectValidado.setItemLabelGenerator(value -> value == 1 ? "Validado" : "No Validado");
        
        selectValidado.addValueChangeListener(event -> { 
            actualizarGrid(fechaInicio.getValue(), fechaFin.getValue(), selectValidado.getValue());
        });
    
        HorizontalLayout fechasLayout = new HorizontalLayout(selectUsuarios, fechaInicio, fechaFin, selectValidado);
        fechasLayout.setAlignItems(Alignment.CENTER);
        fechasLayout.addClassName("fechas-layout");
        fechasLayout.setWidthFull();
        
	    List<Usuario> usuariosDeLaEmpresa = new ArrayList<>();
	
	    if (usuarioActual.getRol() == 1) {
	    	usuariosDeLaEmpresa = usuarioRepositorio.findAll().stream().filter(usuario -> usuario.getRol() == 4 && usuario.getActivo() == 1).collect(Collectors.toList());
	    } else {
	        usuariosDeLaEmpresa = usuarioRepositorio.findByEmpresaId(usuarioActual.getEmpresa().getId()).stream().filter(usuario -> usuario.getRol() == 4 && usuario.getActivo() == 1).collect(Collectors.toList());
	    }
	
	    Usuario general = new Usuario();
	    general.setNombre("General");
	    general.setLoginUsuario("General"); 
	    general.setId(-1);
	    usuariosDeLaEmpresa.add(0, general); 
	    
	    selectUsuarios.setItems(usuariosDeLaEmpresa);
	    selectUsuarios.setItemLabelGenerator(Usuario::getLoginUsuario);
	    selectUsuarios.setPlaceholder("Seleccione un usuario");
	
	    selectUsuarios.addValueChangeListener(event -> {
	        Usuario usuarioSeleccionado = event.getValue();
	        if (usuarioSeleccionado != null) {
	            usuarioActualAux = usuarioSeleccionado;
                actualizarGrid(fechaInicio.getValue(), fechaFin.getValue(), selectValidado.getValue());
                selectValidado.clear();
	        }
	    });
	
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
        Grid.Column<Registro> usuarioColumn = grid.addColumn(registro -> registro.getUsuarioId().getLoginUsuario()).setHeader("Usuario").setAutoWidth(true);
        grid.addColumn(Registro::getObservaciones).setHeader("Observaciones");
        usuarioColumn.setVisible(usuarioActualAux != null && usuarioActualAux.getId() == -1);
        
        selectUsuarios.addValueChangeListener(event -> {
            Usuario usuarioSeleccionado = event.getValue();
            if (usuarioSeleccionado != null) {
                usuarioActualAux = usuarioSeleccionado;
                usuarioColumn.setVisible(usuarioActualAux.getId() == -1);
                if (usuarioActualAux.getId() != -1) {
                    if (contenidoPrincipal.getChildren().noneMatch(c -> c.equals(totalHorasTrabajadasLabel))) {
                        contenidoPrincipal.addComponentAtIndex(1, totalHorasTrabajadasLabel);
                    }
                } else {
                    contenidoPrincipal.remove(totalHorasTrabajadasLabel);
                }
                actualizarGrid(fechaInicio.getValue(), fechaFin.getValue(), selectValidado.getValue());
            }
        });
        
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

                Button actualizar = new Button("Actualizar");
                actualizar.setVisible(false);  
                actualizar.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer");

                Button eliminar = new Button("Eliminar", e -> dialog.close());
                eliminar.getStyle().set("background-color", "red").set("color", "white").set("cursor", "pointer");
                eliminar.addClickListener(e -> {
                	registro.setActivo(0); 
                	registroRepositorio.save(registro);
                    Notification.show("Registro eliminado correctamente", 2000, Notification.Position.TOP_CENTER);
                    actualizarGrid(fechaInicio.getValue(), fechaFin.getValue(), selectValidado.getValue());
                    dialog.close();
                });
                
                Button modificar = new Button("Modificar");
                modificar.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer");

                Button validar = new Button();
	            if (registro.getValidado() == 1) {
	                validar.setText("Validado");
	                validar.getStyle().set("background-color", "green").set("color", "white").set("cursor", "pointer");
	            } else {
	                validar.setText("Validar");
	                validar.getStyle().set("background-color", "green").set("color", "white").set("cursor", "pointer");
	
	                validar.addClickListener(e -> {
	                    registro.setValidado(1); 
	                    registroRepositorio.save(registro);
	                    Notification.show("Registro validado correctamente", 2000, Notification.Position.TOP_CENTER);
	                    actualizarGrid(fechaInicio.getValue(), fechaFin.getValue(), selectValidado.getValue());
	                    dialog.close();
	                });
	            }
             
                modificar.addClickListener(e -> {
                    horaSpan.setVisible(false);
                    horaTextField.setVisible(true);
                    modificar.setVisible(false);
                    validar.setVisible(false);
                    actualizar.setVisible(true);
                    eliminar.setVisible(false);
                });

                actualizar.addClickListener(e -> {
                    LocalTime horaPrevia = registro.getHora();
                    LocalTime nuevaHora = LocalTime.parse(horaTextField.getValue(), DateTimeFormatter.ofPattern("HH:mm:ss"));
                    String horaPreviaStr = horaPrevia != null ? horaPrevia.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "N/A";
                    String nuevaHoraStr = nuevaHora != null ? nuevaHora.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "N/A";
                    registro.setHora(nuevaHora);
                    registro.setValidado(0);
                    registroRepositorio.save(registro);
                    String nombreCampo = "hora"; 
                    registroLog(registro, nombreCampo, horaPreviaStr, nuevaHoraStr, selectValidado.getValue());

                    Notification.show("Registro actualizado correctamente", 2000, Notification.Position.TOP_CENTER);
                    actualizarGrid(fechaInicio.getValue(), fechaFin.getValue(), selectValidado.getValue());
                    dialog.close(); 
                });
                HorizontalLayout botones = new HorizontalLayout(eliminar, modificar, actualizar, validar);
                dialog.add(titulo, contenido, botones);
                dialog.open();
            });
        });

        fechaInicio.addValueChangeListener(event -> actualizarGrid(fechaInicio.getValue(), fechaFin.getValue(), selectValidado.getValue()));
        fechaFin.addValueChangeListener(event -> actualizarGrid(fechaInicio.getValue(), fechaFin.getValue(), selectValidado.getValue()));
        
        Button descargarPdf = new Button("Descargar PDF", e -> generarPdf(grid));
        Button descargarExcel = new Button("Descargar EXCEL", e -> generarExcel(grid));

        HorizontalLayout botonesLayout = new HorizontalLayout(descargarPdf, descargarExcel);
        botonesLayout.setAlignItems(Alignment.CENTER);
        botonesLayout.addClassName("botones-layout");
        botonesLayout.setWidthFull();
        
        descargarPdf.getStyle().set("margin-top", "37px").set("color", "white").set("background-color", "red").set("cursor", "pointer");
        descargarExcel.getStyle().set("margin-top", "37px").set("color", "white").set("background-color", "green").set("cursor", "pointer");

        fechasLayout.add(botonesLayout);

        totalHorasTrabajadasLabel = new Span("");
        totalHorasTrabajadasLabel.getStyle().set("font-weight", "bold").set("font-size", "16px");
        totalHorasTrabajadasLabel.addClassName("horas-trabajadas-label");

        contenidoPrincipal = new VerticalLayout(fechasLayout, grid);
        this.contenidoPrincipal = contenidoPrincipal;
        contenidoPrincipal.setPadding(true);
        contenidoPrincipal.setAlignItems(Alignment.START);
        contenidoPrincipal.setWidthFull();
        contenidoPrincipal.setHeightFull();
        
        setContent(contenidoPrincipal);
        actualizarGrid(fechaInicio.getValue(), fechaFin.getValue(), selectValidado.getValue());
    }


    private void actualizarGrid(LocalDate fechaInicio, LocalDate fechaFin, Integer selectValidado) {
        List<Registro> registros;
        if (usuarioActualAux == null) {
            registros = Collections.emptyList();
            
        } else if (fechaInicio != null && fechaFin != null) {
        	if (selectValidado == null) {
        		if (usuarioActualAux.getId() == -1 && usuarioActual.getRol() == 1) {
        			registros = registroRepositorio.findByFechaRegistroBetweenAndActivo(fechaInicio, fechaFin, 1);
        		} else if (usuarioActualAux.getId() == -1 && usuarioActual.getRol() == 2){
        			registros = registroRepositorio.findByFechaRegistroBetweenAndActivoAndUsuario_Empresa_Id(fechaInicio, fechaFin, 1, usuarioActual.getEmpresa().getId());
        		} else {
        			registros = registroRepositorio.findByFechaRegistroBetweenAndUsuario_IdAndActivo(fechaInicio, fechaFin, usuarioActualAux.getId().intValue(),1);
        		}
        	} else {
        		if (usuarioActualAux.getId() == -1 && usuarioActual.getRol() == 1) {
        			registros = registroRepositorio.findByFechaRegistroBetweenAndActivoAndValidado(fechaInicio, fechaFin, 1, selectValidado);
        		} else if (usuarioActualAux.getId() == -1 && usuarioActual.getRol() == 2){
        			registros = registroRepositorio.findByFechaRegistroBetweenAndActivoAndValidadoAndUsuario_Empresa_Id(fechaInicio, fechaFin, 1, selectValidado, usuarioActual.getEmpresa().getId());
        		} else {
        			registros = registroRepositorio.findByFechaRegistroBetweenAndUsuario_IdAndActivoAndValidado(fechaInicio, fechaFin, usuarioActualAux.getId().intValue(), 1, selectValidado);
        		}
            }
        } else {
        	LocalDate hoy = LocalDate.now();
        	if (selectValidado == null) {
        		if (usuarioActualAux.getId() == -1 && usuarioActual.getRol() == 1) {
        			registros = registroRepositorio.findByFechaRegistroAndActivo(hoy, 1);
        		} else if(usuarioActualAux.getId() == -1 && usuarioActual.getRol() == 2) {
        			registros = registroRepositorio.findByFechaRegistroAndActivoAndUsuario_Empresa_Id(hoy, 1, usuarioActual.getEmpresa().getId());
        		} else {
        			registros = registroRepositorio.findByFechaRegistroAndUsuario_IdAndActivo(hoy, usuarioActualAux.getId().intValue(), 1);
        		}
        	} else {
        		if (usuarioActualAux.getId() == -1 && usuarioActual.getRol() == 1) {
        			registros = registroRepositorio.findByFechaRegistroAndAndActivoAndValidado(hoy, 1, selectValidado);
        		} else if(usuarioActualAux.getId() == -1 && usuarioActual.getRol() == 2) {
        			registros = registroRepositorio.findByFechaRegistroAndAndActivoAndValidadoAndUsuario_Empresa_Id(hoy, 1, selectValidado, usuarioActual.getEmpresa().getId());
        		} else {
        			registros = registroRepositorio.findByFechaRegistroAndUsuario_IdAndActivoAndValidado(hoy, usuarioActualAux.getId().intValue(), 1, selectValidado);
        		}
            }
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
        
        if(usuarioActualAux.getId() == -1) {
        	Paragraph usuarioParrafo = new Paragraph("Usuario: " + usuarioActual.getNombre() + " (" + usuarioActual.getEmpresa().getNombreComercial() + ")").setFontSize(12);
        	document.add(usuarioParrafo);
        } else {
        	Paragraph usuarioParrafo = new Paragraph("Usuario: " + usuarioActualAux.getNombre() + " (" + usuarioActualAux.getEmpresa().getNombreComercial() + ")").setFontSize(12);
        	document.add(usuarioParrafo);
        }

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
        if(usuarioActualAux.getId() == -1) {
        	fechasParrafo.setMarginBottom(15);
        }
        document.add(fechasParrafo);
        
        if(usuarioActualAux.getId() != -1) {
        	Paragraph horasParrafo = new Paragraph("Total trabajado: " + horasTrabajadas + " horas").setFontSize(12).setMarginBottom(15);
            document.add(horasParrafo);
        }
        
        Table table = new Table(6);
        
        table.addHeaderCell(new Cell().add(new Paragraph("FECHA")));
        table.addHeaderCell(new Cell().add(new Paragraph("ACCION")));
        table.addHeaderCell(new Cell().add(new Paragraph("HORA")));
        table.addHeaderCell(new Cell().add(new Paragraph("ORIGEN")));
        if(usuarioActualAux.getId() == -1) {
        	table.addHeaderCell(new Cell().add(new Paragraph("USUARIO")));
        } else {
        	table.addHeaderCell(new Cell().add(new Paragraph("ESTADO")));
        }
        table.addHeaderCell(new Cell().add(new Paragraph("OBSERVACIONES")));

        for (Registro registro : registros) {
        	table.addCell(new Cell().add(new Paragraph(registro.getFechaRegistro().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))));
            table.addCell(new Cell().add(new Paragraph(registro.getAccion())));
            table.addCell(new Cell().add(new Paragraph(registro.getHora() != null ? registro.getHora().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "")));
            table.addCell(new Cell().add(new Paragraph(registro.getOrigen())));
            if(usuarioActualAux.getId() == -1) {
            	table.addCell(new Cell().add(new Paragraph(registro.getUsuarioId().getLoginUsuario())));
            } else {
            	table.addCell(new Cell().add(new Paragraph(registro.getValidado() == 1 ? "VALIDADO" : "NO VALIDADO")));
            }
            table.addCell(new Cell().add(new Paragraph(registro.getObservaciones())));
        }

        document.add(table);
        document.close();

        byte[] pdfBytes = byteArrayOutputStream.toByteArray();
        String base64Pdf = java.util.Base64.getEncoder().encodeToString(pdfBytes);

        String nombreArchivo;
        if (fechaInicioValor.equals(fechaFinValor)) {
        	if(usuarioActualAux.getId() == -1) {
        		nombreArchivo = "FICHAJES_GENERAL_" + fechaInicioFormateada + ".pdf";
            } else {
            	nombreArchivo = "FICHAJES_" + usuarioActualAux.getNombre() + "_" + fechaInicioFormateada + ".pdf";
            }
        } else {
        	if(usuarioActualAux.getId() == -1) {
        		nombreArchivo = "FICHAJES_GENERAL_" + fechaInicioFormateada + "_" + fechaFinFormateada + ".pdf";
            } else {
            	nombreArchivo = "FICHAJES_" + usuarioActualAux.getNombre() + "_" + fechaInicioFormateada + "_" + fechaFinFormateada + ".pdf";
            }
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
        if(usuarioActualAux.getId() == -1) {
        	userRow.createCell(0).setCellValue("Usuario: " + usuarioActual.getNombre() + " (" + usuarioActual.getEmpresa().getNombreComercial() + ")");
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));
        } else {
        	userRow.createCell(0).setCellValue("Usuario: " + usuarioActualAux.getNombre() + " (" + usuarioActualAux.getEmpresa().getNombreComercial() + ")");
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));
        }

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

        if (usuarioActualAux != null && usuarioActualAux.getId() != -1) {
            String horasTrabajadas = calcularHorasTrabajadas(registros);
            XSSFRow hoursRow = sheet.createRow(3);
            hoursRow.createCell(0).setCellValue("Total trabajado: " + horasTrabajadas + " horas");
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 4));
        }

        XSSFRow headerRow = sheet.createRow(4);
        headerRow.createCell(0).setCellValue("FECHA");
        headerRow.createCell(1).setCellValue("ACCION");
        headerRow.createCell(2).setCellValue("HORA");
        headerRow.createCell(3).setCellValue("ORIGEN");
        if (usuarioActualAux.getId() == -1) {
            headerRow.createCell(4).setCellValue("USUARIO");
            headerRow.createCell(5).setCellValue("OBSERVACIONES");
        } else {
            headerRow.createCell(4).setCellValue("ESTADO");
            headerRow.createCell(5).setCellValue("OBSERVACIONES");
        }

        int rowIndex = 5;
        for (Registro registro : registros) {
            XSSFRow row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(registro.getFechaRegistro().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            row.createCell(1).setCellValue(registro.getAccion());
            row.createCell(2).setCellValue(registro.getHora() != null ? registro.getHora().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "");
            row.createCell(3).setCellValue(registro.getOrigen());
            if (usuarioActualAux.getId() == -1) {
                row.createCell(4).setCellValue(registro.getUsuarioId().getLoginUsuario());
                row.createCell(5).setCellValue(registro.getObservaciones());
            } else {
                row.createCell(4).setCellValue(registro.getValidado() == 1 ? "VALIDADO" : "NO VALIDADO");
                row.createCell(5).setCellValue(registro.getObservaciones());
            }
        }

        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }

        String nombreArchivo;
        if (fechaInicioValor.equals(fechaFinValor)) {
        	if(usuarioActualAux.getId() == -1) {
        		nombreArchivo = "FICHAJES_GENERAL_" + fechaInicioFormateada + ".xlsx";
            } else {
            	nombreArchivo = "FICHAJES_" + usuarioActualAux.getNombre() + "_" + fechaInicioFormateada + ".xlsx";
            }
        } else {
            if(usuarioActualAux.getId() == -1) {
        		nombreArchivo = "FICHAJES_GENERAL_" + fechaInicioFormateada + "_" + fechaFinFormateada + ".xlsx";
            } else {
                nombreArchivo = "FICHAJES_" + usuarioActualAux.getNombre() + "_" + fechaInicioFormateada + "_" + fechaFinFormateada + ".xlsx";
            }
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
    
    private void registroLog(Registro registro, String campo, String valorPrevio, String valorNuevo, Integer selectValidado) {
    	Logs_modificaciones registroLog = new Logs_modificaciones();
    	registroLog.setFecha(LocalDate.now());
    	registroLog.setRegistro(registro);
    	registroLog.setCampo(campo);
    	registroLog.setValorPrevio(valorPrevio);
    	registroLog.setValorNuevo(valorNuevo);
    	
    	logs_modificacionesRepositorio.save(registroLog);
    	actualizarGrid(fechaInicio.getValue(), fechaFin.getValue(), selectValidado);
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

        List<Notificaciones> pendientes = notificacionesRepositorio.findByEstado(0);

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
                	texto1 = new Span(permisoNotificacion.getMotivo() + " - " + notificacion.getSolicitante().getLoginUsuario());
                	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                	if (permisoNotificacion.getFechaAux() == null) {
                		texto2 = new Span(permisoNotificacion.getFecha().format(formatter));
                	} else {
                		texto2 = new Span(permisoNotificacion.getFecha().format(formatter) + " - " + permisoNotificacion.getFechaAux().format(formatter));
                	}
                } else {
                	Optional<Solicitudes> solicitudesOptional = solicitudesRepositorio.findById(notificacion.getidAsociado());
                	Solicitudes solicitudNotificacion = solicitudesOptional.get();
                	texto1 = new Span(solicitudNotificacion.getTipo() + " - " + notificacion.getSolicitante().getLoginUsuario());
                	texto2 = new Span(solicitudNotificacion.getRegistro().getFechaRegistro() + " " + solicitudNotificacion.getRegistro().getAccion() + ", "+ solicitudNotificacion.getValorPrevio() + " a " + solicitudNotificacion.getValor());
                }
                
                texto1.getStyle().set("font-weight", "500");
                texto2.getStyle().set("font-weight", "500");

                Image aceptarImagen = new Image("img/si.png", "Aceptar Icono");
                aceptarImagen.setWidth("35px");
                aceptarImagen.setHeight("35px");
                aceptarImagen.getStyle().set("margin-top", "5px");
                Button aceptar = new Button(aceptarImagen, ev -> {
                    notificacion.setEstado(1);
                    notificacion.setResolucion("ACEPTADO");
                    if ("PERMISO".equals(notificacion.getTipo())) {
                    	Optional<Permisos> permisoOptional = permisosRepositorio.findById(notificacion.getidAsociado());
                    	Permisos permisoNotificacion = permisoOptional.get();
                    	permisoNotificacion.setEstado("ACEPTADO");
                    	permisosRepositorio.save(permisoNotificacion); 
                    	emailNotificacion.enviarCorreoRespuestaPermiso(permisoNotificacion.getSolicitante().getNombre(), permisoNotificacion.getSolicitante().getEmail(), permisoNotificacion.getMotivo(), permisoNotificacion.getFecha(), permisoNotificacion.getFechaAux(), permisoNotificacion.getEstado());
                    } else {
                    	Optional<Solicitudes> solicitudesOptional = solicitudesRepositorio.findById(notificacion.getidAsociado());
                    	Solicitudes solicitudNotificacion = solicitudesOptional.get();
                    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                    	LocalTime nuevaHora = LocalTime.parse(solicitudNotificacion.getValor(), formatter);
                    	Registro registro = solicitudNotificacion.getRegistro();
                    	registro.setHora(nuevaHora);
                    	registro.setValidado(1);
                    	solicitudNotificacion.setEstado("ACEPTADO");
                        registroRepositorio.save(registro);
                    	solicitudesRepositorio.save(solicitudNotificacion); 
                    	emailNotificacion.enviarCorreoRespuestaSolicitud(solicitudNotificacion.getSolicitante().getNombre(), solicitudNotificacion.getSolicitante().getEmail(), solicitudNotificacion.getRegistro().getFechaRegistro(), solicitudNotificacion.getAccion(), solicitudNotificacion.getValorPrevio(), solicitudNotificacion.getValor(), solicitudNotificacion.getEstado());
                    }
                    notificacionesRepositorio.save(notificacion);
                    dialog.close(); 
                    UI.getCurrent().getPage().reload();

                });
                aceptar.getStyle().set("background-color", "white").set("cursor", "pointer");
                
                Image rechazarImagen = new Image("img/no.png", "Rechazar Icono");
                rechazarImagen.setWidth("45px");
                rechazarImagen.setHeight("45px");
                rechazarImagen.getStyle().set("margin-top", "5px");
                Button rechazar = new Button(rechazarImagen, ev -> {
                	 notificacion.setEstado(1);
                	 notificacion.setResolucion("RECHAZADO");
                     if ("PERMISO".equals(notificacion.getTipo())) {
                     	Optional<Permisos> permisoOptional = permisosRepositorio.findById(notificacion.getidAsociado());
                     	Permisos permisoNotificacion = permisoOptional.get();
                     	permisoNotificacion.setEstado("RECHAZADO");
                     	permisosRepositorio.save(permisoNotificacion); 
                     	emailNotificacion.enviarCorreoRespuestaPermiso(permisoNotificacion.getSolicitante().getNombre(), permisoNotificacion.getSolicitante().getEmail(), permisoNotificacion.getMotivo(), permisoNotificacion.getFecha(), permisoNotificacion.getFechaAux(), permisoNotificacion.getEstado());
                     } else {
                     	Optional<Solicitudes> solicitudesOptional = solicitudesRepositorio.findById(notificacion.getidAsociado());
                     	Solicitudes solicitudNotificacion = solicitudesOptional.get();
                     	Registro registro = solicitudNotificacion.getRegistro();
                     	registro.setValidado(1);
                     	solicitudNotificacion.setEstado("RECHAZADO");
                        registroRepositorio.save(registro);
                     	solicitudesRepositorio.save(solicitudNotificacion); 
                     	emailNotificacion.enviarCorreoRespuestaSolicitud(solicitudNotificacion.getSolicitante().getNombre(), solicitudNotificacion.getSolicitante().getEmail(), solicitudNotificacion.getRegistro().getFechaRegistro(), solicitudNotificacion.getAccion(), solicitudNotificacion.getValorPrevio(), solicitudNotificacion.getValor(), solicitudNotificacion.getEstado());
                     }
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
                HorizontalLayout botones = new HorizontalLayout(aceptar, rechazar);
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