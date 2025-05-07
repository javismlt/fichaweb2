package com.gts.fichaweb.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
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
import modelos.Usuario;
import repositorios.EmpresaRepositorio;
import repositorios.UsuarioRepositorio;
import java.util.List;
import java.util.stream.Stream;

@Route("modempresa/:id") 
public class ModEmpresa extends AppLayout implements BeforeEnterObserver {

    private final EmpresaRepositorio empresaRepositorio;
    private Empresa empresaActual;
    private final UsuarioRepositorio usuarioRepositorio;
    private Usuario usuarioActual;
    private Usuario usuarioLogueado;

    public ModEmpresa(EmpresaRepositorio empresaRepositorio, UsuarioRepositorio usuarioRepositorio) {
        this.empresaRepositorio = empresaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
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
        if (usuarioLogueado.getRol() != 1) {
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

        TextField campo10 = new TextField("Código GTSERP");
        campo10.setValue(String.valueOf(empresaActual.getCodGtserp()));

        TextField campo11 = new TextField("Grupo GTSERP");
        campo11.setValue(String.valueOf(empresaActual.getGrupoGtserp()));

        TextField campo12 = new TextField("Empresa GTSERP");
        campo12.setValue(String.valueOf(empresaActual.getEmpresaGtserp()));

        Select<String> campo13 = new Select<>();
        campo13.setLabel("Modo Pausa");
        campo13.setItems("Activar", "Desactivar");
        campo13.setValue(empresaActual.getActivoPausa() == 1 ? "Activar" : "Desactivar");

        Stream.of(campo1, campo2, campo3, campo4, campo5, campo6, campo7, campo8, campo9)
            .forEach(tf -> tf.setWidth("300px"));

        if (usuarioLogueado.getRol() == 0) {
            Stream.of(campo10, campo11, campo12, campo13).forEach(tf -> tf.setWidth("300px"));
        }

        Button btnActualizar = new Button("Actualizar");
        btnActualizar.setWidth("100px");
        btnActualizar.setHeight("40px");
        btnActualizar.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer").set("margin-top", "35px").set("margin-left", "100px");

        btnActualizar.addClickListener(e -> {
            if (usuarioLogueado.getRol() == 0) {
                actualizarEmpresa(
                    empresaActual.getId(), campo1.getValue(), campo2.getValue(), campo3.getValue(), campo4.getValue(),
                    campo5.getValue(), campo6.getValue(), campo7.getValue(), campo8.getValue(), campo9.getValue(),
                    campo10.getValue(), campo11.getValue(), campo12.getValue(), campo13.getValue()
                );
            } else {
                actualizarEmpresa(
                    empresaActual.getId(), campo1.getValue(), campo2.getValue(), campo3.getValue(), campo4.getValue(),
                    campo5.getValue(), campo6.getValue(), campo7.getValue(), campo8.getValue(), campo9.getValue(),
                    null, null, null, null
                );
            }
        });

        VerticalLayout columnaIzquierda;
        VerticalLayout columnaDerecha;

        if (usuarioLogueado.getRol() == 0) {
            columnaIzquierda = new VerticalLayout(campo1, campo2, campo3, campo4, campo5, campo6, campo7);
            columnaDerecha = new VerticalLayout(campo8, campo9, campo10, campo11, campo12, campo13, btnActualizar);
        } else {
            columnaIzquierda = new VerticalLayout(campo1, campo2, campo3, campo4, campo5);
            columnaDerecha = new VerticalLayout(campo6, campo7, campo8, campo9, btnActualizar);
        }

        columnaIzquierda.setWidth("45%");
        columnaDerecha.setWidth("45%");

        HorizontalLayout columnasLayout = new HorizontalLayout(columnaIzquierda, columnaDerecha);
        columnasLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        columnasLayout.setAlignItems(Alignment.START);

        VerticalLayout layoutFormulario = new VerticalLayout(new H2("Modificar Empresa"), columnasLayout);
        layoutFormulario.setAlignItems(Alignment.CENTER);
        layoutFormulario.setWidthFull();
        layoutFormulario.getStyle().set("padding", "20px");

        setContent(layoutFormulario);
    }

    private void actualizarEmpresa(int id, String nombreComercial, String razonSocial, String direccion, String codigoPostal, String pais, String provincia, String poblacion, String telefono, String correoElectronico, String codigoGTSERP, String grupoGTSERP, String empresaGTSERP, String modoPausa) {
        int codigo_gtserp = Integer.parseInt(codigoGTSERP);
        int grupo_gtserp = Integer.parseInt(grupoGTSERP);
        int empresa_gtserp = Integer.parseInt(empresaGTSERP);

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
        empresa.setCodGtserp(codigo_gtserp);
        empresa.setGrupoGtserp(grupo_gtserp);
        empresa.setEmpresaGtserp(empresa_gtserp);
        empresa.setActivoPausa("Activar".equals(modoPausa) ? 1 : 0);

        empresaRepositorio.save(empresa);
        Notification.show("Empresa actualizada: " + empresa.getNombreComercial(), 2000, Notification.Position.TOP_CENTER);
        UI.getCurrent().navigate("listempresas");
    }
}
