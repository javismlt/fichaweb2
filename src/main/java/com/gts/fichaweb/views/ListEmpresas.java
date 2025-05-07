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
import modelos.Empresa;
import modelos.Usuario;
import repositorios.EmpresaRepositorio;
import repositorios.UsuarioRepositorio;
import repositorios.RegistroRepositorio;
import servicios.EmpresaServicio;
import org.springframework.transaction.annotation.Transactional;

@Route("listempresas")
public class ListEmpresas extends AppLayout {

	private final EmpresaServicio empresaServicio; 
    private final EmpresaRepositorio empresaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final RegistroRepositorio registroRepositorio;
    private Usuario usuarioActual;

    public ListEmpresas(UsuarioRepositorio usuarioRepositorio, EmpresaRepositorio empresaRepositorio, RegistroRepositorio registroRepositorio, EmpresaServicio empresaServicio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.empresaRepositorio = empresaRepositorio;  
        this.registroRepositorio = registroRepositorio;  
        this.empresaServicio = empresaServicio;

        String nombreUsuario = (String) VaadinSession.getCurrent().getAttribute("username");
        if (nombreUsuario == null) {
            getElement().executeJs("window.location.href='/'");
        } else {
            this.usuarioActual = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
            if (usuarioActual.getRol() == 0 || usuarioActual.getRol() == 1) {
            	crearHeader(nombreUsuario);
            	mostrarEmpresas();
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

        Button botonUsuario = new Button("Añadir Usuario", e -> {
            UI.getCurrent().navigate("addusuario");
        });
        botonUsuario.getStyle().set("color", "white").set("background-color", "#007BFF").set("font-size", "16px").set("border", "1px solid black").set("cursor", "pointer").set("padding", "8px 16px").set("border-radius", "4px");

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

    private void mostrarEmpresas() {
        H2 titulo = new H2("Empresas");
        titulo.getStyle().set("margin-top", "8px").set("text-align", "center");
        List<Empresa> empresas; 
        
        if (usuarioActual.getRol() == 1) {
            Integer empresaId = usuarioActual.getEmpresa().getId();
            Empresa empresa = empresaRepositorio.findById(empresaId).orElseThrow();
            empresas = List.of(empresa);
        } else {
            empresas = empresaRepositorio.findAll();
        }

        VerticalLayout listaEmpresas = new VerticalLayout();
        listaEmpresas.setPadding(true);
        listaEmpresas.setSpacing(true);
        listaEmpresas.setAlignItems(Alignment.CENTER);
        listaEmpresas.setWidthFull();

        for (Empresa empresa : empresas) {
            String textoUsuario = empresa.getNombreComercial() + " (" + empresa.getRazonSocial() + ")";
            Span infoUsuario = new Span(textoUsuario);
            infoUsuario.getStyle()
                .set("font-size", "16px")
                .set("flex-grow", "1")
                .set("text-align", "center");

            Button btnModificar = new Button("Modificar", click -> {
                UI.getCurrent().navigate("modempresa/" + empresa.getId());
            });
            btnModificar.getStyle()
                .set("background-color", "#007BFF")
                .set("color", "white")
                .set("cursor", "pointer")
                .set("border", "none");

            Button btnEliminar = new Button("Eliminar", e -> {
                Confirmacion(empresa);
            });
            btnEliminar.getStyle()
                .set("background-color", "red")
                .set("color", "white")
                .set("cursor", "pointer")
                .set("border", "none");

            Button btnActivar = new Button(empresa.getActivo() == 1 ? "Activo" : "Activar");
            btnActivar.setWidth("90px");
            btnActivar.getStyle()
                .set("background-color", empresa.getActivo() == 1 ? "green" : "#bfbfbf")
                .set("color", "white")
                .set("cursor", "pointer")
                .set("border", "none");
            
            btnActivar.addClickListener(e -> cambiarEstado(empresa, btnActivar));

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

            listaEmpresas.add(filaUsuario);
        }

        VerticalLayout contenedorCentro = new VerticalLayout();
        contenedorCentro.setAlignItems(Alignment.CENTER);
        contenedorCentro.setJustifyContentMode(JustifyContentMode.CENTER);
        contenedorCentro.add(titulo, listaEmpresas);

        setContent(contenedorCentro);
    }

    private void Confirmacion(Empresa empresa) {
        Dialog confirmacionDialogo = new Dialog();
        confirmacionDialogo.setCloseOnEsc(true);
        confirmacionDialogo.setCloseOnOutsideClick(true);

        Span mensaje = new Span("¿Estás seguro de que quieres eliminar la empresa " + empresa.getNombreComercial() + "?");

        VerticalLayout contenidoLayout = new VerticalLayout(mensaje);
        contenidoLayout.getStyle().set("margin-bottom", "5px");

        Button btnEliminar = new Button("Eliminar", e -> {
            empresaServicio.eliminar(empresa); 
            Notification notification = Notification.show("Empresa, usuarios y registros eliminados", 2000, Notification.Position.TOP_CENTER);
            
            notification.addDetachListener(detachEvent -> {
                UI.getCurrent().access(() -> UI.getCurrent().getPage().reload());
            });

            confirmacionDialogo.close();
        });

        btnEliminar.getStyle().set("background-color", "red").set("color", "white").set("border", "none");

        Button btnCancelar = new Button("Cancelar", e -> {
            confirmacionDialogo.close();
        });
        btnCancelar.getStyle().set("background-color", "#007BFF").set("color", "white").set("border", "none");

        HorizontalLayout accionesLayout = new HorizontalLayout(btnEliminar, btnCancelar);
        accionesLayout.setSpacing(true);
        accionesLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        confirmacionDialogo.add(contenidoLayout, accionesLayout);
        confirmacionDialogo.open();
    }
    
    private void cambiarEstado(Empresa empresa, Button boton) {
        if (empresa.getActivo() == 1) {
        	empresa.setActivo(0);
            empresaRepositorio.save(empresa);

            boton.setText("Activar");
            boton.getStyle().set("background-color", "#bfbfbf").set("color", "white");
            Notification.show("Empresa " + empresa.getNombreComercial() + " desactivada", 2000, Notification.Position.TOP_CENTER);
        } else {
        	empresa.setActivo(1);
        	empresaRepositorio.save(empresa);

            boton.setText("Activo");
            boton.getStyle().set("background-color", "green").set("color", "white");
            Notification.show("Empresa " + empresa.getNombreComercial() + " activada", 2000, Notification.Position.TOP_CENTER);
        }
    }
}

