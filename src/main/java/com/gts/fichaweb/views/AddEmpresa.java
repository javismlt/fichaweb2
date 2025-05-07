package com.gts.fichaweb.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import modelos.Empresa;
import modelos.Usuario;
import repositorios.UsuarioRepositorio;
import repositorios.EmpresaRepositorio;
import java.util.stream.Stream;
import com.vaadin.flow.component.select.Select;

@Route("addempresa")
public class AddEmpresa extends AppLayout{
	private final UsuarioRepositorio usuarioRepositorio;
	private final EmpresaRepositorio empresaRepositorio;
    private Usuario usuarioActual;

	public AddEmpresa(UsuarioRepositorio usuarioRepositorio, EmpresaRepositorio empresaRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.empresaRepositorio = empresaRepositorio;

        String nombreUsuario = (String) VaadinSession.getCurrent().getAttribute("username");
        if (nombreUsuario == null) {
        	getElement().executeJs("window.location.href='/'");
        } else {
            this.usuarioActual = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
            if (usuarioActual == null || usuarioActual.getRol() != 0) {
            	Notification.show("Acceso denegado: no tienes permisos suficientes", 2000, Notification.Position.TOP_CENTER);
            	getElement().executeJs("setTimeout(() => window.location.href='/registro', 2000)");
                return;
            }
            crearHeader(nombreUsuario);
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

        HorizontalLayout menuIzquierdo = new HorizontalLayout(botonEmpresa, botonUsuario, enlaceEmpresas, enlaceUsuarios);
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
        crearFormulario();
    }
	
	private void crearFormulario() {
	   
	    H2 titulo = new H2("Añadir Empresa");
	    titulo.getStyle().set("text-align", "center");

	    TextField campo1 = new TextField("Nombre Comercial");
	    TextField campo2 = new TextField("Razón Social");
	    TextField campo3 = new TextField("Dirección");
	    TextField campo4 = new TextField("Codigo Postal");
	    TextField campo5 = new TextField("País");
	    TextField campo6 = new TextField("Provincia");
	    TextField campo7 = new TextField("Población");
	    TextField campo8 = new TextField("Teléfono");
	    TextField campo9 = new TextField("Correo Electrónico");
	    TextField campo10 = new TextField("Código GTSERP");
	    TextField campo11 = new TextField("Grupo GTSERP");
	    TextField campo12 = new TextField("Empresa GTSERP");

	    Select<String> campo13 = new Select<>();
	    campo13.setLabel("Modo Pausa");
	    campo13.setItems("Activar", "Desactivar");
	    campo13.setWidth("300px");
	    
	    Stream.of(campo1, campo2, campo3, campo4, campo5, campo6, campo7, campo8, campo9, campo10, campo11, campo12).forEach(tf -> tf.setWidth("300px"));

	    Button btnGuardar = new Button("Guardar");
	    btnGuardar.setWidth("100px");
	    btnGuardar.setHeight("40px");
	    btnGuardar.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer").set("margin-top", "35px").set("margin-left", "100px");

	    btnGuardar.addClickListener(e -> {
	        boolean guardado = registrarEmpresa(campo1.getValue(), campo2.getValue(), campo3.getValue(), campo4.getValue(), campo5.getValue(), campo6.getValue(), campo7.getValue(), campo8.getValue(), campo9.getValue(), campo10.getValue(), campo11.getValue(), campo12.getValue(), campo13.getValue());

	        if (guardado) {
	            limpiarFormulario(campo1, campo2, campo3, campo4, campo5, campo6, campo7, campo8, campo9, campo10, campo11, campo12, campo13);
	        }
	    });
	    
	    VerticalLayout columnaIzquierda = new VerticalLayout(campo1, campo2, campo3, campo4, campo5, campo6, campo7);
	    VerticalLayout columnaDerecha = new VerticalLayout(campo8, campo9, campo10, campo11, campo12, campo13, btnGuardar);
	    
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
	
	private boolean registrarEmpresa(String nombreComercial, String razonSocial, String direccion, String codPostal, String pais, String provincia, String poblacion, String telefono, String email, String codGtserp, String grupoGtserp, String empresaGtserp, String activoPausa) {
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
	    empresa.setEmpresaGtserp(empresa_gtserp);  
	    empresa.setCodGtserp(codigo_gtserp);  
	    empresa.setGrupoGtserp(grupo_gtserp);  
	    empresa.setActivoPausa("Activar".equals(activoPausa) ? 1 : 0);  
	    
	    empresaRepositorio.save(empresa);
	    Notification.show("Empresa añadida: " + empresa.getNombreComercial(), 2000, Notification.Position.TOP_CENTER);
	    return true;
	}
	
	private void limpiarFormulario(TextField campo1, TextField campo2, TextField campo3, TextField campo4, TextField campo5, TextField campo6, TextField campo7, TextField campo8, TextField campo9, TextField campo10, TextField campo11, TextField campo12, Select<String> campo13) {
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
		campo13.setValue(null);
	}
}
