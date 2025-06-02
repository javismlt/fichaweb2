package com.gts.fichaweb.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import java.util.List;
import modelos.Usuario;
import modelos.Registro; 
import modelos.Empresa;
import modelos.Notificaciones;
import modelos.Solicitudes;
import modelos.Permisos;
import repositorios.EmpresaRepositorio;
import repositorios.SolicitudesRepositorio;
import repositorios.PermisosRepositorio;
import repositorios.UsuarioRepositorio;
import servicios.EmailNotificacion;
import repositorios.NotificacionesRepositorio;
import repositorios.RegistroRepositorio; 
import com.vaadin.flow.component.select.Select;
import java.util.stream.Collectors;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import java.util.Optional;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;
import java.util.Arrays;

@Route("listusuarios")
@CssImport(value = "./themes/my-theme/styles.css", themeFor = "vaadin-grid") 
public class ListUsuarios extends AppLayout {
    private final UsuarioRepositorio usuarioRepositorio;
    private final EmpresaRepositorio empresaRepositorio;
    private final RegistroRepositorio registroRepositorio;  
    private final NotificacionesRepositorio notificacionesRepositorio; 
    private final PermisosRepositorio permisosRepositorio; 
    private final SolicitudesRepositorio solicitudesRepositorio; 
    private Usuario usuarioActual;
    private Span UsuariosActivos; 
    private Button botonUsuario;
    private final EmailNotificacion emailNotificacion;
    
    public ListUsuarios(UsuarioRepositorio usuarioRepositorio, EmpresaRepositorio empresaRepositorio, RegistroRepositorio registroRepositorio, NotificacionesRepositorio notificacionesRepositorio, PermisosRepositorio permisosRepositorio, SolicitudesRepositorio solicitudesRepositorio, EmailNotificacion emailNotificacion) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.empresaRepositorio = empresaRepositorio;
        this.registroRepositorio = registroRepositorio;
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
                mostrarUsuarios();
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
    
    private void mostrarUsuarios() {
        H2 titulo = new H2("Usuarios");
        titulo.getStyle().set("margin-top", "8px").set("text-align", "center");
        
        botonUsuario = new Button("+ Añadir Usuario");
        botonUsuario.addClickListener(e -> {
            UI.getCurrent().navigate("addusuario");
        });
        botonUsuario.getStyle().set("font-size", "12px").set("margin-left", "10px").set("margin-top", "15px").set("background-color", "green").set("color", "white").set("cursor", "pointer").set("border", "none");
        
        int cantidadUsuarios = usuarioRepositorio.countByEmpresaIdAndActivo(usuarioActual.getEmpresa().getId(), 1);
        int maxEmpleados = usuarioActual.getEmpresa().getMaxEmpleados();

        botonUsuario.setVisible(cantidadUsuarios < maxEmpleados);

        HorizontalLayout tituloConBoton = new HorizontalLayout(titulo, botonUsuario);
		tituloConBoton.setAlignItems(Alignment.CENTER);
		tituloConBoton.setJustifyContentMode(JustifyContentMode.CENTER);
		tituloConBoton.setWidthFull();
		tituloConBoton.getStyle().set("margin-bottom", "20px");
		
        VerticalLayout contenedorCentro = new VerticalLayout();
        contenedorCentro.setAlignItems(Alignment.CENTER);
        contenedorCentro.setJustifyContentMode(JustifyContentMode.CENTER);
        contenedorCentro.add(tituloConBoton);

        UsuariosActivos = new Span();
        actualizarCantidadUsuarios();

        if (usuarioActual.getRol() == 2) {
        	UsuariosActivos.getStyle().set("font-size", "16px");
            contenedorCentro.add(UsuariosActivos);
        }

        VerticalLayout listaUsuarios = new VerticalLayout();
        listaUsuarios.setPadding(true);
        listaUsuarios.setSpacing(true);
        listaUsuarios.setAlignItems(Alignment.CENTER); 
        listaUsuarios.setWidthFull();  

        if (usuarioActual.getRol() == 1) {
            Select<Empresa> selectEmpresa = new Select<>();
            List<Empresa> empresas = empresaRepositorio.findAll()
            		.stream()
            	    .filter(empresa -> (empresa.getActivo() == 1))
            	    .collect(Collectors.toList());
            selectEmpresa.setItems(empresas);
            selectEmpresa.setItemLabelGenerator(Empresa::getNombreComercial);
            selectEmpresa.setPlaceholder("Empresa");

            List<Usuario> usuarios = usuarioRepositorio.findAll()
            	    .stream()
            	    .filter(usuario -> (usuario.getRol() == 1))
            	    .collect(Collectors.toList());
            mostrarListaUsuarios(listaUsuarios, usuarios);

            selectEmpresa.addValueChangeListener(event -> {
                Empresa empresaSeleccionada = event.getValue();
                if (empresaSeleccionada != null) {
                    List<Usuario> filtrados = usuarioRepositorio.findByEmpresaId(empresaSeleccionada.getId())
	                    .stream()
	            	    .filter(usuario -> !(usuario.getRol() == 1))
	            	    .collect(Collectors.toList());
                    mostrarListaUsuarios(listaUsuarios, filtrados);
                }
            });
            contenedorCentro.add(selectEmpresa);
        } else {
            List<Usuario> usuarios = usuarioRepositorio.findByEmpresaId(usuarioActual.getEmpresa().getId());
            mostrarListaUsuarios(listaUsuarios, usuarios);
        }

        contenedorCentro.add(listaUsuarios);
        setContent(contenedorCentro);
    }

    
    private void mostrarListaUsuarios(VerticalLayout listaUsuarios, List<Usuario> usuarios) {
        listaUsuarios.removeAll();

        usuarios.sort((u1, u2) -> {
            List<Integer> rolesPrioritarios = Arrays.asList(2, 3, 5);
            boolean u1EsPrioritario = rolesPrioritarios.contains(u1.getRol());
            boolean u2EsPrioritario = rolesPrioritarios.contains(u2.getRol());

            if (u1EsPrioritario && !u2EsPrioritario) {
                return -1;
            } else if (!u1EsPrioritario && u2EsPrioritario) {
                return 1;
            } else {
                return 0;
            }
        });
        
        for (Usuario u : usuarios) {
            String textoUsuario = u.getNombre();
            Span infoUsuario = new Span(textoUsuario);
            infoUsuario.getStyle()
            	.set("font-size", "16px")
            	.set("flex-grow", "1")
            	.set("text-align", "left")
            	.set("width", "100%");

            Button btnModificar = new Button("Modificar", e -> {
                UI.getCurrent().navigate("modusuario/" + u.getId());
            });
            btnModificar.getStyle()
                .set("background-color", "#007BFF")
                .set("color", "white")
                .set("cursor", "pointer")
                .set("border", "none");

            Button btnEliminar = new Button("Eliminar", e -> {
                Confirmacion(u);
            });
            btnEliminar.getStyle()
                .set("background-color", "red")
                .set("color", "white")
                .set("cursor", "pointer")
                .set("border", "none");
            
            Button btnActivar = new Button(u.getActivo() == 1 ? "Activo" : "Activar");
            btnActivar.setWidth("90px");
            btnActivar.getStyle()
                .set("background-color", u.getActivo() == 1 ? "green" : "#bfbfbf")
                .set("color", "white")
                .set("cursor", "pointer")
                .set("border", "none");

            btnActivar.addClickListener(e -> cambiarEstado(u, btnActivar));

            HorizontalLayout botones = new HorizontalLayout();
            botones.setSpacing(true);
            botones.setAlignItems(Alignment.CENTER);
            botones.getStyle().set("flex-grow", "0");
            botones.setJustifyContentMode(JustifyContentMode.END);

            if ((usuarioActual.getRol() == 1 || usuarioActual.getRol() == 2) && !usuarioActual.getId().equals(u.getId())) {
            	    botones.add(btnEliminar);
            }
            botones.add(btnModificar);
            botones.add(btnActivar);

            HorizontalLayout filaUsuario = new HorizontalLayout(infoUsuario, botones);
            filaUsuario.setWidthFull();
            filaUsuario.setAlignItems(Alignment.CENTER);
            filaUsuario.setJustifyContentMode(JustifyContentMode.START);
            filaUsuario.getStyle()
                .set("max-width", "800px")
                .set("margin", "0 auto");

            listaUsuarios.add(filaUsuario);
        }
    }

    private void Confirmacion(Usuario usuario) {
        Dialog confirmacionDialogo = new Dialog();
        confirmacionDialogo.setCloseOnEsc(true);
        confirmacionDialogo.setCloseOnOutsideClick(true);

        Span mensaje = new Span("¿Estás seguro de que quieres eliminar al usuario " + usuario.getNombre() + "?");

        VerticalLayout contenidoLayout = new VerticalLayout(mensaje);
        contenidoLayout.getStyle().set("margin-bottom", "5px");

        Button btnEliminar = new Button("Eliminar", e -> {
            eliminarRegistros(usuario);

            usuarioRepositorio.deleteById(usuario.getId());
            Notification notification = Notification.show("Usuario eliminado", 1500, Notification.Position.TOP_CENTER);

            notification.addDetachListener(detachEvent -> {
                UI.getCurrent().access(() -> UI.getCurrent().getPage().reload());
            });

            confirmacionDialogo.close();
        });
        btnEliminar.getStyle().set("cursor", "pointer").set("background-color", "red").set("color", "white").set("border", "none");

        Button btnCancelar = new Button("Cancelar", e -> {
            confirmacionDialogo.close();
        });
        btnCancelar.getStyle().set("cursor", "pointer").set("background-color", "#007BFF").set("color", "white").set("border", "none");

        HorizontalLayout accionesLayout = new HorizontalLayout(btnEliminar, btnCancelar);
        accionesLayout.setSpacing(true);
        accionesLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        confirmacionDialogo.add(contenidoLayout, accionesLayout);
        confirmacionDialogo.open();
    }

    private void eliminarRegistros(Usuario usuario) {
        List<Registro> registros = registroRepositorio.findByUsuario_Id(usuario.getId());
        for (Registro registro : registros) {
            registroRepositorio.delete(registro); 
        }
    }
    
    private void cambiarEstado(Usuario usuario, Button btnActivar) {
        int nuevoEstado = usuario.getActivo() == 1 ? 0 : 1;
        usuario.setActivo(nuevoEstado);
        usuarioRepositorio.save(usuario);
        
        if (usuario.getRol() == 3) {
            Empresa empresa = usuario.getEmpresa();
            empresa.setMultiusuario(nuevoEstado);
            empresaRepositorio.save(empresa);
        }
        
        if (usuario.getRol() == 5) {
            Empresa empresa = usuario.getEmpresa();
            empresa.setInspector(nuevoEstado);
            empresaRepositorio.save(empresa);
        }

        String texto = nuevoEstado == 1 ? "Activo" : "Activar";
        String color = nuevoEstado == 1 ? "green" : "#bfbfbf";
        String mensaje = "Usuario " + usuario.getNombre() + (nuevoEstado == 1 ? " activado" : " desactivado");

        btnActivar.setText(texto);
        btnActivar.getStyle().set("background-color", color).set("color", "white");

        Notification.show(mensaje, 1000, Notification.Position.TOP_CENTER);

        actualizarCantidadUsuarios();
    }

    private void actualizarCantidadUsuarios() {
        int cantidadUsuarios = usuarioRepositorio.countByEmpresaIdAndActivo(usuarioActual.getEmpresa().getId(), 1);
        int maxEmpleados = usuarioActual.getEmpresa().getMaxEmpleados();
        UsuariosActivos.setText("Usuarios " + cantidadUsuarios + " de " + maxEmpleados);

        if (cantidadUsuarios >= maxEmpleados && usuarioActual.getRol() == 2) {
            botonUsuario.setVisible(false); 
        } else {
            botonUsuario.setVisible(true);
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