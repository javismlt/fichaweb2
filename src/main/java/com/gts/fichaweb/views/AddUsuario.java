package com.gts.fichaweb.views;

import java.util.stream.Stream;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import modelos.Empresa;
import modelos.Usuario;
import repositorios.UsuarioRepositorio;
import servicios.EmailNotificacion;
import repositorios.EmpresaRepositorio;
import modelos.Usuario;
import repositorios.EmpresaRepositorio;
import repositorios.UsuarioRepositorio;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; 
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.io.InputStream;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.server.StreamResource;
import java.io.ByteArrayInputStream;
import com.vaadin.flow.component.html.Image;
import modelos.Notificaciones;
import modelos.Solicitudes;
import modelos.Permisos;
import modelos.Registro;
import repositorios.NotificacionesRepositorio;
import repositorios.SolicitudesRepositorio;
import repositorios.PermisosRepositorio;
import repositorios.RegistroRepositorio;

@Route("addusuario")
@CssImport(value = "./themes/my-theme/styles.css", themeFor = "vaadin-grid") 
public class AddUsuario extends AppLayout{
	private final UsuarioRepositorio usuarioRepositorio;
	private final EmpresaRepositorio empresaRepositorio;
    private Usuario usuarioActual;
    private Upload upload;
    private final NotificacionesRepositorio notificacionesRepositorio; 
    private final PermisosRepositorio permisosRepositorio; 
    private final SolicitudesRepositorio solicitudesRepositorio; 
    private final RegistroRepositorio registroRepositorio; 
    private final EmailNotificacion emailNotificacion;
    
	public AddUsuario(RegistroRepositorio registroRepositorio, UsuarioRepositorio usuarioRepositorio, EmpresaRepositorio empresaRepositorio, NotificacionesRepositorio notificacionesRepositorio, PermisosRepositorio permisosRepositorio, SolicitudesRepositorio solicitudesRepositorio, EmailNotificacion emailNotificacion) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.empresaRepositorio = empresaRepositorio;
        this.notificacionesRepositorio = notificacionesRepositorio;
        this.permisosRepositorio = permisosRepositorio;
        this.solicitudesRepositorio = solicitudesRepositorio;
        this.registroRepositorio = registroRepositorio;
        this.emailNotificacion = emailNotificacion;
        
        String nombreUsuario = (String) VaadinSession.getCurrent().getAttribute("username");
        if (nombreUsuario == null) {
        	getElement().executeJs("window.location.href='/'");
        } else {
            this.usuarioActual = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
            if (usuarioActual.getRol() == 1 || usuarioActual.getRol() == 2) {
            	crearHeader(nombreUsuario);
            	crearFormulario();
            } else {
            	Notification.show("Usuario no administrador", 2000, Notification.Position.TOP_CENTER);
                getElement().executeJs("setTimeout(() => window.location.href='/registro', 2000)");
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
	
	private void crearFormulario() {
		H2 titulo = new H2("Añadir Usuario");
		titulo.getStyle().set("text-align", "center");

		Image icono = new Image("img/config.png", "Icono");
		icono.setWidth("40px");
		icono.setHeight("40px");

		Button botonImportar = new Button(icono);
		botonImportar.getStyle().set("margin-left", "10px").set("margin-top", "20px").set("background-color", "white").set("color", "white").set("cursor", "pointer").set("border", "none");

		upload = importar();
		
		ContextMenu menuImportar = new ContextMenu(botonImportar);
		menuImportar.setOpenOnClick(true);

		Span plantillaSpan = new Span("Descargar Plantilla");
		plantillaSpan.getStyle().set("color", "red").set("cursor", "pointer");

		Span importarSpan = new Span("Importar Usuarios");
		importarSpan.getStyle().set("color", "green").set("cursor", "pointer");

		Anchor enlaceCSV = DescargaPlantilla();
		
		menuImportar.addItem(plantillaSpan, e -> {
			enlaceCSV.getElement().executeJs("this.click();");
		});

		menuImportar.addItem(importarSpan, e -> {
		    upload.getElement().executeJs("this.shadowRoot.querySelector('input[type=file]').click();");
		});

		HorizontalLayout tituloConBoton = new HorizontalLayout(titulo, botonImportar);
		tituloConBoton.setAlignItems(Alignment.CENTER);
		tituloConBoton.setJustifyContentMode(JustifyContentMode.CENTER);
		tituloConBoton.setWidthFull();
		tituloConBoton.getStyle().set("margin-bottom", "10px");

	    TextField campo1 = new TextField("Nombre *");
	    TextField campo2 = new TextField("Teléfono");
	    campo2.setMaxLength(9);
	    TextField campo3 = new TextField("Correo Electrónico *");
	    TextField campo4 = new TextField("NIF");
	    campo4.setMaxLength(9);
	    TextField campo5 = new TextField("Usuario login *");
	    PasswordField  campo6 = new PasswordField ("Contraseña *");
	    TextField campo7 = new TextField("Codigo Personal");
	    TextField campo8 = new TextField("Pin");
	    campo8.setMaxLength(4);

	    Select<String> campo9 = new Select<>();
	    campo9.setLabel("Rol *");
	    campo9.setWidth("300px");

	    final int[] rolSeleccionado = new int[1];

	    if (usuarioActual.getRol() == 2) {
	        campo9.setItems("Trabajador");
	    } else {
	        campo9.setItems("Administrador", "Trabajador");
	    }

	    campo9.addValueChangeListener(event -> {
	        String selectedRole = event.getValue();
	        if ("Administrador".equals(selectedRole)) {
	            rolSeleccionado[0] = 1;  
	        } else if ("Trabajador".equals(selectedRole)) {
	            rolSeleccionado[0] = 4; 
	        }
	    });
	    
	    Select<String> campo10 = new Select<>();
	    campo10.setLabel("Fichaje Manual *");
	    campo10.setItems("Activar", "Desactivar");
	    campo10.setWidth("300px");
	    
	    Select<String> campo11 = new Select<>();
	    campo11.setLabel("Empresa *");
	    campo11.setWidth("300px");

	    if (usuarioActual.getRol() == 2) {
	        Empresa empresaUsuario = usuarioActual.getEmpresa();
	        String nombreEmpresa = empresaUsuario.getNombreComercial();
	        campo11.setItems(nombreEmpresa); 
	        campo11.setValue(nombreEmpresa); 
	    } else {
	        List<String> nombresEmpresas = empresaRepositorio.findAll()
	                .stream()
	                .map(Empresa::getNombreComercial)
	                .toList();

	        campo11.setItems(nombresEmpresas);
	    }

	    Stream.of(campo1, campo2, campo3, campo4, campo5, campo6, campo7, campo8, campo9, campo10, campo11).forEach(tf -> tf.setWidth("300px"));
	    
	    Button btnGuardar = new Button("Guardar");
	    btnGuardar.setWidth("100px");
	    btnGuardar.setHeight("40px");
	    btnGuardar.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer").set("margin-top", "35px").set("margin-left", "100px");

	    btnGuardar.addClickListener(e -> {
	        boolean guardado = registrarUsuario(campo1.getValue(), campo2.getValue(), campo3.getValue(), campo4.getValue(),campo5.getValue(), campo6.getValue(), campo7.getValue(), campo8.getValue(),rolSeleccionado[0], campo10.getValue(), campo11.getValue());
	        if (guardado) {
	        	 UI.getCurrent().navigate("listusuarios");
	        }
	    });
	    
	    VerticalLayout columnaIzquierda = new VerticalLayout(campo1, campo2, campo3, campo4, campo5, campo6);
	    VerticalLayout columnaDerecha = new VerticalLayout(campo7, campo8, campo9, campo10, campo11,btnGuardar);
	    
	    columnaIzquierda.setWidth("45%");
	    columnaDerecha.setWidth("45%");
	    
	    HorizontalLayout columnasLayout = new HorizontalLayout(columnaIzquierda, columnaDerecha);
	    columnasLayout.setJustifyContentMode(JustifyContentMode.CENTER);
	    columnasLayout.setAlignItems(Alignment.START);
	    columnasLayout.addClassName("form-columns");

	    VerticalLayout layoutFormulario = new VerticalLayout(tituloConBoton, columnasLayout, upload, enlaceCSV);
	    layoutFormulario.setAlignItems(Alignment.CENTER);
	    layoutFormulario.setWidthFull();
	    layoutFormulario.getStyle().set("padding", "20px");

	    setContent(layoutFormulario);
	}
	
	
	private boolean registrarUsuario(String Nombre, String Telefono, String email, String nif, String usuarioLogin, String password, String codPersonal, String pin, int rol, String fichajeManual, String empresa) {
		Usuario usuario = new Usuario();
		usuario.setNombre(Nombre);
		usuario.setTelefono(Telefono);
		usuario.setEmail(email);
		usuario.setNif(nif);
		usuario.setLoginUsuario(usuarioLogin);
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String passwordEncriptada = passwordEncoder.encode(password);
		usuario.setPassword(passwordEncriptada);
		
	    Empresa empresaObj = empresaRepositorio.findByNombreComercial(empresa).orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
	    usuario.setEmpresa(empresaObj);

	    Integer codPersonalInt = null;

	    if (codPersonal != null && !codPersonal.trim().isEmpty()) {
	        try {
	            codPersonalInt = Integer.parseInt(codPersonal);
	        } catch (NumberFormatException e) {
	            Notification.show("El valor de código personal no es un número válido.", 2000, Notification.Position.TOP_CENTER);
	            return false;
	        }
	    }

	    boolean existeCodPersonal = false;
	    if (codPersonalInt != null) {
	        existeCodPersonal = usuarioRepositorio.existsByCodPersonalAndEmpresa_Id(codPersonalInt, empresaObj.getId());
	        if (existeCodPersonal) {
	            Notification.show("El código personal ya existe para esta empresa.", 2000, Notification.Position.TOP_CENTER);
	            return false;
	        }
	        usuario.setCodPersonal(codPersonalInt);
	    } else {
	        usuario.setCodPersonal(null);
	    }

		usuario.setPin(pin);
		usuario.setRol(rol);
		usuario.setFichajeManual("Activar".equals(fichajeManual) ? 1 : 0);    
		usuario.setCreado(LocalDateTime.now());

		try {
	        usuarioRepositorio.save(usuario);
	        Notification.show("Usuario añadido: " + usuario.getNombre(), 2000, Notification.Position.TOP_CENTER);
	        return true;
	    } catch (Exception e) {
	        Notification.show("Usuario Login ya existente", 2000, Notification.Position.TOP_CENTER);
	        return false;
	    }
	}
	
	/*private void limpiarFormulario(TextField campo1, TextField campo2, TextField campo3, TextField campo4, TextField campo5, PasswordField  campo6, TextField campo7, TextField campo8, Select<String> campo9, Select<String> campo10, Select<String> campo11) {
		campo1.setValue("");
		campo2.setValue("");
		campo3.setValue("");
		campo4.setValue("");
		campo5.setValue("");
		campo6.setValue("");
		campo7.setValue("");
		campo8.setValue("");
		campo9.setValue(null);
		campo10.setValue(null);
		campo11.setValue(null);
	}*/
	
	private Upload importar() {
	    MemoryBuffer buffer = new MemoryBuffer();
	    Upload upload = new Upload(buffer);
	    upload.setAcceptedFileTypes(".csv");
	    upload.getElement().setAttribute("hidden", true);
	    upload.getElement().executeJs(
	        "const btn = document.querySelector('vaadin-button[theme~=\"primary\"]');" +
	        "if (btn) btn.addEventListener('click', () => this.inputElement.click());"
	    );

	    upload.addSucceededListener(event -> {
	        try (InputStream inputStream = buffer.getInputStream();
	            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
	        	
	            String linea = reader.readLine(); 

	            while ((linea = reader.readLine()) != null) {
	                String[] partes = linea.split(";");  

	                if (partes.length < 8) {  
	                    Notification.show("Línea inválida: " + linea, 2000, Notification.Position.MIDDLE);
	                    continue;
	                }

	                String nombre = partes[0].trim();
	                String telefono = partes[1].trim();
	                String email = partes[2].trim();
	                String nif = partes[3].trim();
	                String usuarioLogin = partes[4].trim();
	                String password = partes[5].trim();
	                String codPersonal = partes[6].trim();
	                String pin = partes[7].trim();

	                registrarUsuario(nombre, telefono, email, nif, usuarioLogin, password, codPersonal, pin, 4, "1", usuarioActual.getEmpresa().getNombreComercial());
	            }
	            Notification.show("Importación completada", 2000, Notification.Position.TOP_CENTER);
	        } catch (IOException ex) {
	            Notification.show("Error al leer el archivo", 2000, Notification.Position.TOP_CENTER);
	        }
	    });
	    return upload;
	}
	
	private Anchor DescargaPlantilla() {
	    String csvContent = "Nombre;Telefono;Email;Nif;UsuarioLogin;Password;CodPersonal;Pin\n";

	    StreamResource csvResource = new StreamResource("plantilla.csv", () ->
	            new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8))
	    );

	    Anchor csvDescarga = new Anchor(csvResource, "Descargar Plantilla");
	    csvDescarga.getElement().setAttribute("download", "plantilla.csv");
	    csvDescarga.getStyle().set("display", "none");  

	    return csvDescarga;
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
                    	emailNotificacion.enviarCorreoRespuestaPermiso(permisoNotificacion.getSolicitante().getEmpresa().getId(), permisoNotificacion.getSolicitante().getNombre(), permisoNotificacion.getSolicitante().getEmail(), permisoNotificacion.getMotivo(), permisoNotificacion.getFecha(), permisoNotificacion.getFechaAux(), permisoNotificacion.getEstado());
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
                    	emailNotificacion.enviarCorreoRespuestaSolicitud(solicitudNotificacion.getSolicitante().getEmpresa().getId(), solicitudNotificacion.getSolicitante().getNombre(), solicitudNotificacion.getSolicitante().getEmail(), solicitudNotificacion.getRegistro().getFechaRegistro(), solicitudNotificacion.getAccion(), solicitudNotificacion.getValorPrevio(), solicitudNotificacion.getValor(), solicitudNotificacion.getEstado());
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
                     	emailNotificacion.enviarCorreoRespuestaPermiso(permisoNotificacion.getSolicitante().getEmpresa().getId(), permisoNotificacion.getSolicitante().getNombre(), permisoNotificacion.getSolicitante().getEmail(), permisoNotificacion.getMotivo(), permisoNotificacion.getFecha(), permisoNotificacion.getFechaAux(), permisoNotificacion.getEstado());
                     } else {
                     	Optional<Solicitudes> solicitudesOptional = solicitudesRepositorio.findById(notificacion.getidAsociado());
                     	Solicitudes solicitudNotificacion = solicitudesOptional.get();
                     	Registro registro = solicitudNotificacion.getRegistro();
                     	registro.setValidado(1);
                     	solicitudNotificacion.setEstado("RECHAZADO");
                        registroRepositorio.save(registro);
                     	solicitudesRepositorio.save(solicitudNotificacion); 
                     	emailNotificacion.enviarCorreoRespuestaSolicitud(solicitudNotificacion.getSolicitante().getEmpresa().getId(), solicitudNotificacion.getSolicitante().getNombre(), solicitudNotificacion.getSolicitante().getEmail(), solicitudNotificacion.getRegistro().getFechaRegistro(), solicitudNotificacion.getAccion(), solicitudNotificacion.getValorPrevio(), solicitudNotificacion.getValor(), solicitudNotificacion.getEstado());
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