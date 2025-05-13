package com.gts.fichaweb.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
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
import repositorios.EmpresaRepositorio;
import repositorios.UsuarioRepositorio;
import repositorios.RegistroRepositorio; 
import com.vaadin.flow.component.select.Select;
import java.util.stream.Collectors;

@Route("listusuarios")
public class ListUsuarios extends AppLayout {

    private final UsuarioRepositorio usuarioRepositorio;
    private final EmpresaRepositorio empresaRepositorio;
    private final RegistroRepositorio registroRepositorio;  
    private Usuario usuarioActual;
    private Span UsuariosActivos; 
    private Button botonUsuario;

    public ListUsuarios(UsuarioRepositorio usuarioRepositorio, EmpresaRepositorio empresaRepositorio, RegistroRepositorio registroRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.empresaRepositorio = empresaRepositorio;
        this.registroRepositorio = registroRepositorio;  

        String nombreUsuario = (String) VaadinSession.getCurrent().getAttribute("username");
        if (nombreUsuario == null) {
            getElement().executeJs("window.location.href='/'");
        } else {
            this.usuarioActual = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
            if (usuarioActual.getRol() == 0 || usuarioActual.getRol() == 1) {
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
        Button botonEmpresa = new Button("Añadir Empresa", e -> {
            UI.getCurrent().navigate("addempresa");
        });
        botonEmpresa.getStyle().set("color", "white").set("background-color", "#007BFF").set("font-size", "16px").set("border", "1px solid black").set("cursor", "pointer").set("border-radius", "4px");

        int cantidadUsuarios = usuarioRepositorio.countByEmpresaIdAndActivo(usuarioActual.getEmpresa().getId(), 1);
        int maxEmpleados = usuarioActual.getEmpresa().getMaxEmpleados();
        
        botonUsuario = new Button("Añadir Usuario", e -> {
            UI.getCurrent().navigate("addusuario");
        });
        if (cantidadUsuarios >= maxEmpleados) {
            botonUsuario.setEnabled(false); 
            botonUsuario.getStyle().set("background-color", "#bfbfbf"); 
        } else {
            botonUsuario.getStyle().set("color", "white").set("background-color", "#007BFF").set("font-size", "16px").set("border", "1px solid black").set("cursor", "pointer").set("padding", "8px 16px").set("border-radius", "4px");
        }

        Anchor enlaceEmpresas = new Anchor("usuario", "Empresas");
        enlaceEmpresas.getElement().setAttribute("href", "/listempresas");
        enlaceEmpresas.getStyle().set("color", "black").set("text-decoration", "none").set("font-size", "16px");
        
        Anchor enlaceUsuarios = new Anchor("usuario", "Usuarios");
        enlaceUsuarios.getElement().setAttribute("href", "/listusuarios");
        enlaceUsuarios.getStyle().set("color", "black").set("text-decoration", "none").set("font-size", "16px");

        HorizontalLayout menuIzquierdo = new HorizontalLayout();
        if (usuarioActual.getRol() != 1) {
            menuIzquierdo.add(botonEmpresa);
        }
        
        menuIzquierdo.add(botonUsuario, enlaceEmpresas, enlaceUsuarios);
        menuIzquierdo.setSpacing(true);
        menuIzquierdo.setAlignItems(Alignment.CENTER);

        Button menuDerecho = new Button(nombreUsuario);
        menuDerecho.getStyle().set("color", "black").set("font-size", "16px").set("cursor", "pointer").set("border", "1px solid black").set("border-radius", "4px");

        ContextMenu contextMenu = new ContextMenu(menuDerecho);
        contextMenu.setOpenOnClick(true);
        contextMenu.addItem("Cerrar sesión", e -> {
            UI.getCurrent().access(() -> {
                VaadinSession.getCurrent().close();
                UI.getCurrent().access(() -> {
                    UI.getCurrent().navigate("");
                });
            });
        });

        HorizontalLayout header = new HorizontalLayout(menuIzquierdo, menuDerecho);
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
        
        VerticalLayout contenedorCentro = new VerticalLayout();
        contenedorCentro.setAlignItems(Alignment.CENTER);
        contenedorCentro.setJustifyContentMode(JustifyContentMode.CENTER);
        contenedorCentro.add(titulo);

        UsuariosActivos = new Span();
        actualizarCantidadUsuarios();

        if (usuarioActual.getRol() == 1) {
        	UsuariosActivos.getStyle().set("font-size", "16px");
            contenedorCentro.add(UsuariosActivos);
        }

        VerticalLayout listaUsuarios = new VerticalLayout();
        listaUsuarios.setPadding(true);
        listaUsuarios.setSpacing(true);
        listaUsuarios.setAlignItems(Alignment.CENTER); 
        listaUsuarios.setWidthFull();  

        if (usuarioActual.getRol() == 0) {
            Select<Empresa> selectEmpresa = new Select<>();
            List<Empresa> empresas = empresaRepositorio.findAll();
            selectEmpresa.setItems(empresas);
            selectEmpresa.setItemLabelGenerator(Empresa::getNombreComercial);
            selectEmpresa.setPlaceholder("Empresa");

            List<Usuario> usuarios = usuarioRepositorio.findAll()
            	    .stream()
            	    .filter(usuario -> !(usuario.getRol() == 3 && usuario.getActivo() == 0))
            	    .collect(Collectors.toList());
            mostrarListaUsuarios(listaUsuarios, usuarios);

            selectEmpresa.addValueChangeListener(event -> {
                Empresa empresaSeleccionada = event.getValue();
                if (empresaSeleccionada != null) {
                    List<Usuario> filtrados = usuarioRepositorio.findByEmpresaId(empresaSeleccionada.getId());
                    mostrarListaUsuarios(listaUsuarios, filtrados);
                } else {
                    List<Usuario> todos = usuarioRepositorio.findAll();
                    mostrarListaUsuarios(listaUsuarios, todos);
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

        for (Usuario u : usuarios) {
            String textoUsuario = u.getNombre();
            Span infoUsuario = new Span(textoUsuario);
            infoUsuario.getStyle()
                .set("font-size", "16px")
                .set("flex-grow", "1")
                .set("text-align", "center");

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

            HorizontalLayout botones = new HorizontalLayout(btnModificar, btnEliminar, btnActivar);
            botones.setSpacing(true);
            botones.setAlignItems(Alignment.CENTER);
            botones.getStyle().set("flex-grow", "0");

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

        if (cantidadUsuarios >= maxEmpleados && usuarioActual.getRol() == 1) {
            botonUsuario.setEnabled(false); 
            botonUsuario.getStyle().set("background-color", "#bfbfbf"); 
        } else {
            botonUsuario.setEnabled(true);
            botonUsuario.getStyle().set("color", "white").set("background-color", "#007BFF").set("font-size", "16px").set("border", "1px solid black").set("cursor", "pointer").set("padding", "8px 16px").set("border-radius", "4px");
        }
    }
}