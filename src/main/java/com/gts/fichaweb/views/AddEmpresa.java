package com.gts.fichaweb.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import modelos.Empresa;
import modelos.Notificaciones;
import modelos.Permisos;
import modelos.Registro;
import modelos.Solicitudes;
import modelos.Usuario;
import modelos.Roles;
import repositorios.RolesRepositorio;
import repositorios.UsuarioRepositorio;
import servicios.EmailNotificacion;
import repositorios.EmpresaRepositorio;
import modelos.Notificaciones;
import modelos.Solicitudes;
import modelos.Permisos;
import repositorios.NotificacionesRepositorio;
import repositorios.SolicitudesRepositorio;
import repositorios.PermisosRepositorio;
import repositorios.RegistroRepositorio;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import com.vaadin.flow.component.select.Select;
import java.time.LocalDateTime;

@Route("addempresa")
@CssImport(value = "./themes/my-theme/styles.css", themeFor = "vaadin-grid") 
public class AddEmpresa extends AppLayout{
	private final UsuarioRepositorio usuarioRepositorio;
	private final EmpresaRepositorio empresaRepositorio;
    private Usuario usuarioActual;
    private Empresa ultimaEmpresaCreada;
    private final NotificacionesRepositorio notificacionesRepositorio; 
    private final PermisosRepositorio permisosRepositorio; 
    private final SolicitudesRepositorio solicitudesRepositorio; 
    private final RegistroRepositorio registroRepositorio; 
    private final RolesRepositorio rolesRepositorio; 
    private final EmailNotificacion emailNotificacion;
    
	public AddEmpresa(UsuarioRepositorio usuarioRepositorio, EmpresaRepositorio empresaRepositorio, RegistroRepositorio registroRepositorio, NotificacionesRepositorio notificacionesRepositorio, PermisosRepositorio permisosRepositorio, SolicitudesRepositorio solicitudesRepositorio, EmailNotificacion emailNotificacion, RolesRepositorio rolesRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.empresaRepositorio = empresaRepositorio;
        this.notificacionesRepositorio = notificacionesRepositorio;
        this.permisosRepositorio = permisosRepositorio;
        this.solicitudesRepositorio = solicitudesRepositorio;
        this.registroRepositorio = registroRepositorio;
        this.rolesRepositorio = rolesRepositorio;
        this.emailNotificacion = emailNotificacion;
        
        String nombreUsuario = (String) VaadinSession.getCurrent().getAttribute("username");
        if (nombreUsuario == null) {
        	getElement().executeJs("window.location.href='/'");
        } else {
            this.usuarioActual = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
            if (usuarioActual == null || usuarioActual.getRol() != 1) {
            	Notification.show("Acceso denegado: no tienes permisos suficientes", 2000, Notification.Position.TOP_CENTER);
            	getElement().executeJs("setTimeout(() => window.location.href='/registro', 2000)");
                return;
            }
            crearHeader(nombreUsuario);
            crearFormulario();
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
	
	private void crearFormulario() {
	    H2 titulo = new H2("Añadir Empresa");
	    titulo.getStyle().set("text-align", "center");

	    TextField campo1 = new TextField("Nombre Comercial *");
	    TextField campo2 = new TextField("Razón Social *");
	    TextField campo3 = new TextField("Dirección *");
	    TextField campo4 = new TextField("Codigo Postal *");
	    TextField campo5 = new TextField("País *");
	    TextField campo6 = new TextField("Provincia *");
	    TextField campo7 = new TextField("Población *");
	    TextField campo8 = new TextField("Teléfono *");
	    TextField campo9 = new TextField("Correo Electrónico *");
	    TextField campo10 = new TextField("Máximo Empleados *");
	    TextField campo11 = new TextField("Código GTSERP *");
	    TextField campo12 = new TextField("Grupo GTSERP *");
	    TextField campo13 = new TextField("Empresa GTSERP *");

	    Select<String> campo14 = new Select<>();
	    campo14.setLabel("Multiusuario *");
	    campo14.setItems("Activar", "Desactivar");
	    
	    Select<String> campo15 = new Select<>();
	    campo15.setLabel("Inspector *");
	    campo15.setItems("Activar", "Desactivar");
	    
	    Stream.of(campo1, campo2, campo3, campo4, campo5, campo6, campo7, campo8, campo9, campo10, campo11, campo12, campo13, campo14, campo15).forEach(tf -> tf.setWidth("300px"));

	    Button btnGuardar = new Button("Guardar");
	    btnGuardar.setWidth("100px");
	    btnGuardar.setHeight("40px");
	    btnGuardar.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer").set("text-align", "center").set("margin-top", "35px").set("margin-left", "100px");

	    btnGuardar.addClickListener(e -> {
	        boolean guardado = registrarEmpresa(campo1.getValue(), campo2.getValue(), campo3.getValue(), campo4.getValue(), campo5.getValue(), campo6.getValue(), campo7.getValue(), campo8.getValue(), campo9.getValue(), campo10.getValue(), campo11.getValue(), campo12.getValue(), campo13.getValue(), campo14.getValue(), campo15.getValue());
	        if (guardado && ultimaEmpresaCreada != null) {
	        	Roles rolSupervisor = rolesRepositorio.findById(2).orElseThrow(); 
	        	Usuario supervisor = usuarioRepositorio.findByRolAndEmpresa_Id(rolSupervisor, ultimaEmpresaCreada.getId());
	        	UI.getCurrent().navigate("modusuario/" + supervisor.getId());
	        }
	    });
	    
	    VerticalLayout columnaIzquierda = new VerticalLayout(campo1, campo2, campo3, campo4, campo5, campo6, campo7, campo8);
	    VerticalLayout columnaDerecha = new VerticalLayout(campo9, campo10, campo11, campo12, campo13, campo14, campo15, btnGuardar);
	    
	    columnaIzquierda.setWidth("45%");
	    columnaDerecha.setWidth("45%");
	    
	    HorizontalLayout columnasLayout = new HorizontalLayout(columnaIzquierda, columnaDerecha);
	    columnasLayout.setJustifyContentMode(JustifyContentMode.CENTER);
	    columnasLayout.setAlignItems(Alignment.START);

	    VerticalLayout layoutFormulario = new VerticalLayout(titulo, columnasLayout);
	    layoutFormulario.setAlignItems(Alignment.CENTER);
	    layoutFormulario.setWidthFull();
	    layoutFormulario.getStyle().set("padding", "20px");

	    setContent(layoutFormulario);
	}
	
	
	private boolean registrarEmpresa(String nombreComercial, String razonSocial, String direccion, String codPostal, String pais, String provincia, String poblacion, String telefono, String email, String empleados,String codGtserp, String grupoGtserp, String empresaGtserp, String multiusuario, String inspector) {
	    int codigo_gtserp = Integer.parseInt(codGtserp);
	    int grupo_gtserp = Integer.parseInt(grupoGtserp);
	    int empresa_gtserp = Integer.parseInt(empresaGtserp);

	    if (empresaRepositorio.existsByCodGtserpAndGrupoGtserpAndEmpresaGtserp(codigo_gtserp, grupo_gtserp, empresa_gtserp)) {
	        Notification.show("Ya existe una empresa con este Código, Grupo y Empresa GTSERP. Por favor, introduce una combinación diferente.", 2000, Notification.Position.TOP_CENTER);
	        return false;
	    }

	    Empresa empresa = new Empresa();
	    empresa.setNombreComercial(nombreComercial);
	    empresa.setRazonSocial(razonSocial);
	    empresa.setDireccion(direccion);
	    empresa.setCodPostal(codPostal);
	    empresa.setPais(pais);
	    empresa.setProvincia(provincia);
	    empresa.setPoblacion(poblacion);
	    empresa.setTelefono(telefono);
	    empresa.setEmail(email);
	    empresa.setMaxEmpleados(Integer.parseInt(empleados));
	    empresa.setEmpresaGtserp(empresa_gtserp);  
	    empresa.setCodGtserp(codigo_gtserp);  
	    empresa.setGrupoGtserp(grupo_gtserp);  
	    empresa.setMultiusuario("Activar".equals(multiusuario) ? 1 : 0);  
	    empresa.setInspector("Activar".equals(inspector) ? 1 : 0);  
	    
	    empresaRepositorio.save(empresa);
	    Notification.show("Empresa añadida: " + empresa.getNombreComercial(), 2000, Notification.Position.TOP_CENTER);
	    
	    if ("Activar".equals(multiusuario)) {
	        registrarUsuarioMultiusuario(empresa);
	    }
	    
	    if ("Activar".equals(inspector)) {
	        registrarUsuarioInspector(empresa);
	    }
	    
	    registrarUsuarioSupervisor(empresa);
	    ultimaEmpresaCreada = empresa;
	    return true;
	}
	
	/*private void limpiarFormulario(TextField campo1, TextField campo2, TextField campo3, TextField campo4, TextField campo5, TextField campo6, TextField campo7, TextField campo8, TextField campo9, TextField campo10, TextField campo11, TextField campo12, TextField campo13, Select<String> campo14, Select<String> campo15) {
		campo1.setValue("");
		campo2.setValue("");
		campo3.setValue("");
		campo4.setValue("");
		campo5.setValue("");
		campo6.setValue("");
		campo7.setValue("");
		campo8.setValue("");
		campo9.setValue("");
		campo10.setValue("");
		campo11.setValue("");
		campo12.setValue("");
		campo13.setValue("");
		campo14.setValue(null);
		campo15.setValue(null);
	}*/
	
	private void registrarUsuarioSupervisor(Empresa empresa) {
	    Usuario usuario = new Usuario();
	    usuario.setEmail("");
	    usuario.setNombre("Supervisor_" + empresa.getNombreComercial());
	    usuario.setLoginUsuario("Supervisor_" + empresa.getId());
	    
	    String aleatoriaPassword = generarContraseña();
	    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	    String passwordEncriptada = passwordEncoder.encode(aleatoriaPassword);
	    usuario.setPassword(passwordEncriptada);
	    
	    usuario.setEmpresa(empresa);
	    usuario.setRol(2);
	    usuario.setCreado(LocalDateTime.now());

	    usuarioRepositorio.save(usuario);
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