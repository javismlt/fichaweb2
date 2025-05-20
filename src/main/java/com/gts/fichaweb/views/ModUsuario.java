package com.gts.fichaweb.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import modelos.Empresa;
import modelos.Usuario;
import repositorios.EmpresaRepositorio;
import repositorios.UsuarioRepositorio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Route("modusuario/:id") 
@CssImport(value = "./themes/my-theme/styles.css", themeFor = "vaadin-grid") 
public class ModUsuario extends AppLayout implements BeforeEnterObserver {
    private final UsuarioRepositorio usuarioRepositorio;
    private final EmpresaRepositorio empresaRepositorio;
    private Usuario usuarioActual;
    private Usuario usuarioLogueado;
    private String nombreUsuario;
    private Anchor botonUsuario;
    
    public ModUsuario(UsuarioRepositorio usuarioRepositorio, EmpresaRepositorio empresaRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.empresaRepositorio = empresaRepositorio;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String idParam = event.getRouteParameters().get("id").orElse(null);

        if (idParam == null) {
            Notification.show("No se proporcionó un ID válido en la URL", 2000, Notification.Position.TOP_CENTER);
            getElement().executeJs("window.location.href='/listusuarios'");
            return;
        }

        nombreUsuario = (String) VaadinSession.getCurrent().getAttribute("username");
        this.usuarioLogueado = usuarioRepositorio.findByLoginUsuario(nombreUsuario);

        if (nombreUsuario == null) {
            getElement().executeJs("window.location.href='/'");
        } else {
            Usuario usuario = null;
            try {
                usuario = usuarioRepositorio.findById(Integer.parseInt(idParam)).orElse(null);
            } catch (NumberFormatException e) {
                Notification.show("El ID del usuario no es válido", 2000, Notification.Position.TOP_CENTER);
                getElement().executeJs("window.location.href='/listusuarios'"); 
                return;
            }

            if (usuario == null) {
                Notification.show("No se encontró el usuario con ID: " + idParam, 2000, Notification.Position.TOP_CENTER);
                getElement().executeJs("window.location.href='/listusuarios'"); 
                return;
            }

            this.usuarioActual = usuario;
            crearHeader(nombreUsuario);
            cargarDatosFormulario(); 
        }
    }

    private void crearHeader(String nombreUsuario) {
    	Anchor botonEmpresa = new Anchor("empresa", "Añadir Empresa");
        botonEmpresa.getElement().setAttribute("href", "/addempresa");
        botonEmpresa.getStyle().set("color", "black").set("text-decoration", "none").set("font-size", "16px");
        
        int cantidadUsuarios = usuarioRepositorio.countByEmpresaIdAndActivo(usuarioActual.getEmpresa().getId(), 1);
        int maxEmpleados = usuarioActual.getEmpresa().getMaxEmpleados();
        
        botonUsuario = new Anchor("usuario", "Añadir Usuario");
        botonUsuario.getElement().setAttribute("href", "/addusuario");
        botonUsuario.getStyle().set("color", "black").set("text-decoration", "none").set("font-size", "16px");
        
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
        if (usuarioLogueado.getRol() == 1) {
            menuIzquierdo.add(botonEmpresa);
        }
        
        menuIzquierdo.add(botonUsuario, enlaceEmpresas, enlaceUsuarios, enlaceRegistros);
        menuIzquierdo.setSpacing(true);
        menuIzquierdo.getStyle().set("gap", "25px");
        menuIzquierdo.setAlignItems(Alignment.CENTER);

        Button menuDesplegable = new Button("☰"); 
        menuDesplegable.getStyle().set("font-size", "24px").set("background", "none").set("border", "1px solid black").set("cursor", "pointer").set("border-radius", "4px").set("display", "none");

        ContextMenu menuResponsive = new ContextMenu(menuDesplegable);
        menuResponsive.setOpenOnClick(true);
        if (usuarioActual.getRol() == 1) {
        	menuResponsive.addItem("Añadir Empresa", e -> UI.getCurrent().navigate("addempresa"));
        }
        var itemAddUsuario = menuResponsive.addItem("Añadir Usuario", e -> UI.getCurrent().navigate("addusuario"));
        menuResponsive.addItem("Empresas", e -> UI.getCurrent().navigate("listempresas"));
        menuResponsive.addItem("Usuarios", e -> UI.getCurrent().navigate("listusuarios"));
        menuResponsive.addItem("Registros", e -> UI.getCurrent().navigate("modregistros"));
        
        if (cantidadUsuarios >= maxEmpleados) {
            botonUsuario.setVisible(false); 
            itemAddUsuario.setEnabled(false);
        } else {
        	botonUsuario.setVisible(true); 
        	itemAddUsuario.setEnabled(true);
        }
        
        menuIzquierdo.getElement().getClassList().add("menu-izquierdo");
        menuDesplegable.getElement().getClassList().add("menu-desplegable");
        
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

        HorizontalLayout header = new HorizontalLayout(menuIzquierdo, menuDesplegable, menuDerecho);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        header.setPadding(true);
        header.getStyle().set("padding-top", "10px").set("padding-bottom", "10px").set("padding-left", "100px").set("padding-right", "100px").set("background-color", "#f8f9fa").set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)");

        addToNavbar(header);
    }

    private void cargarDatosFormulario() {
        if (usuarioActual == null) {
            Notification.show("Error: Usuario no encontrado", 3000, Notification.Position.TOP_CENTER);
            return;
        }

        TextField campo1 = new TextField("Nombre *");
        campo1.setValue(usuarioActual.getNombre() != null ? usuarioActual.getNombre() : "");

        TextField campo2 = new TextField("Teléfono");
        campo2.setValue(usuarioActual.getTelefono() != null ? usuarioActual.getTelefono() : "");
        campo2.setMaxLength(9);

        TextField campo3 = new TextField("Correo Electrónico *");
        campo3.setValue(usuarioActual.getEmail() != null ? usuarioActual.getEmail() : "");

        TextField campo4 = new TextField("NIF");
        campo4.setValue(usuarioActual.getNif() != null ? usuarioActual.getNif() : "");
        campo4.setMaxLength(9);

        TextField campo5 = new TextField("Usuario login *");
        campo5.setValue(usuarioActual.getLoginUsuario());

        PasswordField campo6 = new PasswordField("Contraseña *");

        TextField campo7 = new TextField("Codigo Personal");
        campo7.setValue(usuarioActual.getCodPersonal() != null ? String.valueOf(usuarioActual.getCodPersonal()) : "");

        TextField campo8 = new TextField("Pin");
        campo8.setValue(usuarioActual.getPin() != null ? usuarioActual.getPin() : "");
        campo8.setMaxLength(4);

        Select<String> campo9 = new Select<>();
        campo9.setLabel("Rol *");
        campo9.setItems("Trabajador");
        campo9.setValue(getRol(usuarioActual.getRol()));

        Select<String> campo10 = new Select<>();
        campo10.setLabel("Fichaje Manual *");
        campo10.setItems("Activar", "Desactivar");
        campo10.setValue(usuarioActual.getFichajeManual() == 1 ? "Activar" : "Desactivar");

        Select<String> campo11 = new Select<>();
        campo11.setLabel("Empresa *");
        campo11.setWidth("300px");

        if (usuarioLogueado.getRol() == 2) {
            Empresa empresaUsuario = usuarioLogueado.getEmpresa();
            String nombreEmpresa = empresaUsuario.getNombreComercial();
            campo11.setItems(nombreEmpresa);
            campo11.setValue(nombreEmpresa);
        } else {
            List<String> nombresEmpresas = empresaRepositorio.findAll()
                    .stream()
                    .map(Empresa::getNombreComercial)
                    .toList();
            campo11.setItems(nombresEmpresas);
            String nombreEmpresaUsuario = usuarioActual.getEmpresa().getNombreComercial();
            campo11.setValue(nombreEmpresaUsuario);
        }

        Stream.of(campo1, campo2, campo3, campo4, campo5, campo6, campo7, campo8, campo9, campo10, campo11).forEach(tf -> tf.setWidth("300px"));

        Button btnActualizar = new Button("Actualizar");
        btnActualizar.setWidth("100px");
        btnActualizar.setHeight("40px");
        btnActualizar.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer");
        if(usuarioActual.getRol() != 3) {
        	btnActualizar.getStyle().set("margin-top", "35px").set("margin-left", "100px");
        }
        
        btnActualizar.addClickListener(e -> {
        	boolean actualizado = actualizarUsuario(usuarioActual.getId(), campo1.getValue(), campo2.getValue(), campo3.getValue(), campo4.getValue(), campo5.getValue(), campo6.getValue(), campo7.getValue(), campo8.getValue(), getRolNumerico(campo9.getValue()), campo10.getValue(), campo11.getValue());
        	if (actualizado) {
	        	 UI.getCurrent().navigate("listusuarios");
	        }
        });
        
        if (usuarioActual.getRol() == 2 || usuarioActual.getRol() == 3 || usuarioActual.getRol() == 5) {
        	campo5.setReadOnly(true);
            VerticalLayout columnaIzquierda = new VerticalLayout(campo5);
            VerticalLayout columnaDerecha = new VerticalLayout(campo6);
            columnaIzquierda.setWidth("45%");
            columnaDerecha.setWidth("45%");
            
            HorizontalLayout columnasLayout = new HorizontalLayout(columnaIzquierda, columnaDerecha);
            columnasLayout.setJustifyContentMode(JustifyContentMode.CENTER);
            columnasLayout.setAlignItems(Alignment.START);

            VerticalLayout layoutFormulario = new VerticalLayout(new H2("Modificar Usuario"), columnasLayout, btnActualizar);
            layoutFormulario.setAlignItems(Alignment.CENTER);
            layoutFormulario.setWidthFull();
            layoutFormulario.getStyle().set("padding", "20px");

            setContent(layoutFormulario);
            return;
        }

        VerticalLayout columnaIzquierda = new VerticalLayout(campo1, campo2, campo3, campo4, campo5, campo6);
        VerticalLayout columnaDerecha = new VerticalLayout(campo7, campo8, campo9, campo10, campo11, btnActualizar);

        columnaIzquierda.setWidth("45%");
        columnaDerecha.setWidth("45%");

        HorizontalLayout columnasLayout = new HorizontalLayout(columnaIzquierda, columnaDerecha);
        columnasLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        columnasLayout.setAlignItems(Alignment.START);

        VerticalLayout layoutFormulario = new VerticalLayout(new H2("Modificar Usuario"), columnasLayout);
        layoutFormulario.setAlignItems(Alignment.CENTER);
        layoutFormulario.setWidthFull();
        layoutFormulario.getStyle().set("padding", "20px");

        setContent(layoutFormulario);
    }
    
    
    private boolean actualizarUsuario(int id, String Nombre, String Telefono, String email, String nif, String usuarioLogin, String password, String codPersonal, String pin, int rol, String fichajeManual, String empresa) {
        Usuario usuario = usuarioRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (Nombre != null) {
            usuario.setNombre(Nombre);
        }
        if (Telefono != null) {
            usuario.setTelefono(Telefono);
        }
        if (email != null) {
            usuario.setEmail(email);
        }
        if (nif != null) {
            usuario.setNif(nif);
        }

        if (usuarioLogin != null && !usuarioLogin.isEmpty()) {
            usuario.setLoginUsuario(usuarioLogin);
        }

        if (password != null && !password.isEmpty()) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String passwordEncriptada = passwordEncoder.encode(password);
            usuario.setPassword(passwordEncriptada);
        }

        if (codPersonal != null && !codPersonal.isEmpty()) {
            try {
                usuario.setCodPersonal(Integer.parseInt(codPersonal)); 
            } catch (NumberFormatException e) {
                System.out.println("Error: codPersonal no es un número válido");
            }
        }

        if (pin != null) {
            usuario.setPin(pin);
        }
        if (rol >= 0) { 
            usuario.setRol(rol);
        }
        if (fichajeManual != null) {
            usuario.setFichajeManual("Activar".equals(fichajeManual) ? 1 : 0);
        }
        if (empresa != null) {
            Empresa empresaObj = empresaRepositorio.findByNombreComercial(empresa)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
            usuario.setEmpresa(empresaObj);
        }

        usuario.setActualizado(LocalDateTime.now());  
        try {
	        usuarioRepositorio.save(usuario);
	        Notification.show("Usuario añadido: " + usuario.getNombre(), 2000, Notification.Position.TOP_CENTER);
	        if(usuarioActual.getId() == usuarioLogueado.getId()) {
	        	 VaadinSession.getCurrent().setAttribute("username", usuarioLogin);
	        }
	        return true;
	    } catch (Exception e) {
	        Notification.show("Usuario Login ya existente", 2000, Notification.Position.TOP_CENTER);
	        return false;
	    }
    }

    private String getRol(int rol) {
        switch (rol) {
            case 1: return "Administrador";
            case 2: return "Supervisor";
            case 3: return "Multiusuario";
            case 4: return "Trabajador";
            default: return "Desconocido";
        }
    }

    private int getRolNumerico(String rol) {
        switch (rol) {
            case "Administrador": return 1;
            case "Supervisor": return 2;
            case "Multiusuario": return 3;
            case "Trabajador": return 4;
            default: return -1;
        }
    }
}
