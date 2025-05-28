package com.gts.fichaweb.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import modelos.Empresa;
import modelos.Notificaciones;
import modelos.Permisos;
import modelos.Registro;
import modelos.Solicitudes;
import modelos.Usuario;
import repositorios.EmpresaRepositorio;
import repositorios.UsuarioRepositorio;
import servicios.EmailNotificacion;
import repositorios.RegistroRepositorio;
import modelos.Notificaciones;
import modelos.Solicitudes;
import modelos.Permisos;
import repositorios.NotificacionesRepositorio;
import repositorios.SolicitudesRepositorio;
import repositorios.PermisosRepositorio;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;

@Route("modempresa/:id") 
@CssImport(value = "./themes/my-theme/styles.css", themeFor = "vaadin-grid") 
public class ModEmpresa extends AppLayout implements BeforeEnterObserver {
    private final EmpresaRepositorio empresaRepositorio;
    private Empresa empresaActual;
    private final UsuarioRepositorio usuarioRepositorio;
    private Usuario usuarioActual;
    private Usuario usuarioLogueado;
    private final NotificacionesRepositorio notificacionesRepositorio; 
    private final PermisosRepositorio permisosRepositorio; 
    private final SolicitudesRepositorio solicitudesRepositorio; 
    private final RegistroRepositorio registroRepositorio; 
    private final EmailNotificacion emailNotificacion;
    
    public ModEmpresa(EmpresaRepositorio empresaRepositorio, UsuarioRepositorio usuarioRepositorio, RegistroRepositorio registroRepositorio, NotificacionesRepositorio notificacionesRepositorio, PermisosRepositorio permisosRepositorio, SolicitudesRepositorio solicitudesRepositorio, EmailNotificacion emailNotificacion) {
        this.empresaRepositorio = empresaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.notificacionesRepositorio = notificacionesRepositorio;
        this.permisosRepositorio = permisosRepositorio;
        this.solicitudesRepositorio = solicitudesRepositorio;
        this.registroRepositorio = registroRepositorio;
        this.emailNotificacion = emailNotificacion;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String idParam = event.getRouteParameters().get("id").orElse(null);

        if (idParam == null) {
            Notification.show("No se proporcionó un ID válido en la URL", 2000, Notification.Position.TOP_CENTER);
            getElement().executeJs("window.location.href='/listempresas'");
            return;
        }

        String nombreUsuario = (String) VaadinSession.getCurrent().getAttribute("username");
        Usuario usuarioLogueado = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
    		this.usuarioLogueado = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
        if (nombreUsuario == null) {
            getElement().executeJs("window.location.href='/'");
            return;
        }

        this.usuarioActual = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
        if (usuarioActual == null) {
            Notification.show("Usuario no encontrado", 2000, Notification.Position.TOP_CENTER);
            getElement().executeJs("window.location.href='/'");
            return;
        }

        Empresa empresa = null;
        try {
            empresa = empresaRepositorio.findById(Integer.parseInt(idParam)).orElse(null);
        } catch (NumberFormatException e) {
            Notification.show("El ID de la empresa no es válido", 2000, Notification.Position.TOP_CENTER);
            getElement().executeJs("window.location.href='/listempresas'");
            return;
        }

        if (empresa == null) {
            Notification.show("No se encontró la empresa con ID: " + idParam, 2000, Notification.Position.TOP_CENTER);
            getElement().executeJs("window.location.href='/listempresas'");
            return;
        }

        this.empresaActual = empresa;
        crearHeader(nombreUsuario);
        cargarDatosFormulario();
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

    private void cargarDatosFormulario() {
        TextField campo1 = new TextField("Nombre Comercial");
        campo1.setValue(empresaActual.getNombreComercial());

        TextField campo2 = new TextField("Razón Social");
        campo2.setValue(empresaActual.getRazonSocial());

        TextField campo3 = new TextField("Dirección");
        campo3.setValue(empresaActual.getDireccion());

        TextField campo4 = new TextField("Código Postal");
        campo4.setValue(empresaActual.getCodPostal());

        TextField campo5 = new TextField("País");
        campo5.setValue(empresaActual.getPais());

        TextField campo6 = new TextField("Provincia");
        campo6.setValue(empresaActual.getProvincia());

        TextField campo7 = new TextField("Población");
        campo7.setValue(empresaActual.getPoblacion());

        TextField campo8 = new TextField("Teléfono");
        campo8.setValue(empresaActual.getTelefono());

        TextField campo9 = new TextField("Correo Electrónico");
        campo9.setValue(empresaActual.getEmail());
        
        TextField campo10 = new TextField("Máximo Empleados");
        campo10.setValue(String.valueOf(empresaActual.getMaxEmpleados()));

        TextField campo11 = new TextField("Código GTSERP");
        campo11.setValue(String.valueOf(empresaActual.getCodGtserp()));

        TextField campo12 = new TextField("Grupo GTSERP");
        campo12.setValue(String.valueOf(empresaActual.getGrupoGtserp()));

        TextField campo13 = new TextField("Empresa GTSERP");
        campo13.setValue(String.valueOf(empresaActual.getEmpresaGtserp()));

        Select<String> campo14 = new Select<>();
        campo14.setLabel("Multiusuario");
        campo14.setItems("Activar", "Desactivar");
        campo14.setValue(empresaActual.getMultiusuario() == 1 ? "Activar" : "Desactivar");
        
        Select<String> campo15 = new Select<>();
        campo15.setLabel("Inspector");
        campo15.setItems("Activar", "Desactivar");
        campo15.setValue(empresaActual.getInspector() == 1 ? "Activar" : "Desactivar");

        Stream.of(campo1, campo2, campo3, campo4, campo5, campo6, campo7, campo8, campo9, campo14, campo15)
            .forEach(tf -> tf.setWidth("300px"));

        if (usuarioLogueado.getRol() == 1) {
            Stream.of(campo10, campo11, campo12, campo13).forEach(tf -> tf.setWidth("300px"));
        }

        Button btnActualizar = new Button("Actualizar");
        btnActualizar.setWidth("100px");
        btnActualizar.setHeight("40px");
        btnActualizar.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer").set("margin-top", "35px").set("margin-left", "100px");
        
        btnActualizar.addClickListener(e -> {
            if (usuarioLogueado.getRol() == 1) {
                actualizarEmpresa(empresaActual.getId(), campo1.getValue(), campo2.getValue(), campo3.getValue(), campo4.getValue(), campo5.getValue(), campo6.getValue(), campo7.getValue(), campo8.getValue(), campo9.getValue(), campo10.getValue(), campo11.getValue(), campo12.getValue(), campo13.getValue(), campo14.getValue(), campo15.getValue());
            } else {
            	String maxEmpleados = String.valueOf(empresaActual.getMaxEmpleados());
            	String codGtserp = String.valueOf(empresaActual.getCodGtserp());
            	String grupoGtserp = String.valueOf(empresaActual.getGrupoGtserp());
            	String empresaGtserp = String.valueOf(empresaActual.getEmpresaGtserp());
                actualizarEmpresa(empresaActual.getId(), campo1.getValue(), campo2.getValue(), campo3.getValue(), campo4.getValue(), campo5.getValue(), campo6.getValue(), campo7.getValue(), campo8.getValue(), campo9.getValue(), maxEmpleados, codGtserp, grupoGtserp, empresaGtserp, campo14.getValue(), campo15.getValue());
            }
        });

        VerticalLayout columnaDerecha;
        VerticalLayout columnaIzquierda;
        VerticalLayout layoutFormulario = new VerticalLayout();

        if (usuarioLogueado.getRol() == 1) {
            columnaIzquierda = new VerticalLayout(campo1, campo2, campo3, campo4, campo5, campo6, campo7, campo8);
            columnaDerecha = new VerticalLayout(campo9, campo10, campo11, campo12, campo13, campo14, campo15, btnActualizar);
            HorizontalLayout columnasLayout = new HorizontalLayout(columnaIzquierda, columnaDerecha);
            columnasLayout.setJustifyContentMode(JustifyContentMode.CENTER);
            columnasLayout.setAlignItems(Alignment.START);

            layoutFormulario.add(new H2("Modificar Empresa"), columnasLayout);
        } else {
            columnaIzquierda = new VerticalLayout(campo1, campo2, campo3, campo4, campo5, campo6);
            columnaDerecha = new VerticalLayout(campo7, campo8, campo9, campo14, campo15);

            HorizontalLayout columnasLayout = new HorizontalLayout(columnaIzquierda, columnaDerecha);
            columnasLayout.setJustifyContentMode(JustifyContentMode.CENTER);
            columnasLayout.setAlignItems(Alignment.START);

            columnaDerecha.add(btnActualizar);  
            layoutFormulario.add(new H2("Modificar Empresa"), columnasLayout);
        }

        columnaIzquierda.setWidth("45%");
        columnaDerecha.setWidth("45%");

        layoutFormulario.setAlignItems(Alignment.CENTER);
        layoutFormulario.setWidthFull();
        layoutFormulario.getStyle().set("padding", "20px");

        setContent(layoutFormulario);
    }

    private void actualizarEmpresa(int id, String nombreComercial, String razonSocial, String direccion, String codigoPostal, String pais, String provincia, String poblacion, String telefono, String correoElectronico, String maxEmpleados, String codigoGTSERP, String grupoGTSERP, String empresaGTSERP, String multiusuario, String inspector) {
        int codigo_gtserp = Integer.parseInt(codigoGTSERP);
        int grupo_gtserp = Integer.parseInt(grupoGTSERP);
        int empresa_gtserp = Integer.parseInt(empresaGTSERP);
        int max_emleados = Integer.parseInt(maxEmpleados);

        Empresa empresa = empresaRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        if (empresa.getCodGtserp() != codigo_gtserp || empresa.getGrupoGtserp() != grupo_gtserp || empresa.getEmpresaGtserp() != empresa_gtserp) {
            boolean existeCombinacion = empresaRepositorio.existsByCodGtserpAndGrupoGtserpAndEmpresaGtserp(codigo_gtserp, grupo_gtserp, empresa_gtserp);
            if (existeCombinacion) {
                Notification.show("Error duplicado", 3000, Notification.Position.TOP_CENTER);
                return;
            }
        }

        empresa.setNombreComercial(nombreComercial);
        empresa.setRazonSocial(razonSocial);
        empresa.setDireccion(direccion);
        empresa.setCodPostal(codigoPostal);
        empresa.setPais(pais);
        empresa.setProvincia(provincia);
        empresa.setPoblacion(poblacion);
        empresa.setTelefono(telefono);
        empresa.setEmail(correoElectronico);
        empresa.setMaxEmpleados(max_emleados);
        empresa.setCodGtserp(codigo_gtserp);
        empresa.setGrupoGtserp(grupo_gtserp);
        empresa.setEmpresaGtserp(empresa_gtserp);
        empresa.setMultiusuario("Activar".equals(multiusuario) ? 1 : 0);
        empresa.setInspector("Activar".equals(inspector) ? 1 : 0);

        if (empresa.getMultiusuario() == 0) {
            Optional<Usuario> usuarios = usuarioRepositorio.findByEmpresa_IdAndRol_Id(empresa.getId(), 3);
            usuarios.ifPresent(usuario -> {
                usuario.setActivo(0);
                usuarioRepositorio.save(usuario);
            });
        } else {
            Optional<Usuario> usuarios = usuarioRepositorio.findByEmpresa_IdAndRol_Id(empresa.getId(), 3);
            if (!usuarios.isPresent()) {
                registrarUsuarioMultiusuario(empresa);
            } else {
                usuarios.ifPresent(usuario -> {
                    usuario.setActivo(1);
                    usuarioRepositorio.save(usuario);
                });
            }
        }
        
        if (empresa.getInspector() == 0) {
            Optional<Usuario> usuarios = usuarioRepositorio.findByEmpresa_IdAndRol_Id(empresa.getId(), 5);
            usuarios.ifPresent(usuario -> {
                usuario.setActivo(0);
                usuarioRepositorio.save(usuario);
            });
        } else {
            Optional<Usuario> usuarios = usuarioRepositorio.findByEmpresa_IdAndRol_Id(empresa.getId(), 5);
            if (!usuarios.isPresent()) {
                registrarUsuarioInspector(empresa);
            } else {
                usuarios.ifPresent(usuario -> {
                    usuario.setActivo(1);
                    usuarioRepositorio.save(usuario);
                });
            }
        }
        
        empresaRepositorio.save(empresa);
        Notification.show("Empresa actualizada: " + empresa.getNombreComercial(), 2000, Notification.Position.TOP_CENTER);
        UI.getCurrent().navigate("listempresas");
    }
    
    private void registrarUsuarioMultiusuario(Empresa empresa) {
	    Usuario usuario = new Usuario();
	    usuario.setEmail("0");
	    usuario.setNombre("Multiusuario_" + empresa.getNombreComercial());
	    usuario.setLoginUsuario("Multi_" + empresa.getId());
	    
	    String aleatoriaPassword = generarContraseña();
	    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	    String passwordEncriptada = passwordEncoder.encode(aleatoriaPassword);
	    usuario.setPassword(passwordEncriptada);
	    
	    usuario.setEmpresa(empresa);
	    usuario.setRol(3);
	    usuario.setCreado(LocalDateTime.now());

	    usuarioRepositorio.save(usuario);
	}
    
    private void registrarUsuarioInspector(Empresa empresa) {
	    Usuario usuario = new Usuario();
	    usuario.setEmail("0");
	    usuario.setNombre("Inspector_" + empresa.getNombreComercial());
	    usuario.setLoginUsuario("Inspector_" + empresa.getId());
	    
	    String aleatoriaPassword = generarContraseña();
	    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	    String passwordEncriptada = passwordEncoder.encode(aleatoriaPassword);
	    usuario.setPassword(passwordEncriptada);
	    
	    usuario.setEmpresa(empresa);
	    usuario.setRol(5);
	    usuario.setCreado(LocalDateTime.now());

	    usuarioRepositorio.save(usuario);
	}

	private String generarContraseña() {
	    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	    SecureRandom random = new SecureRandom();
	    StringBuilder password = new StringBuilder(10);

	    for (int i = 0; i < 10; i++) {
	        int index = random.nextInt(chars.length());
	        password.append(chars.charAt(index));
	    }
	    return password.toString();
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