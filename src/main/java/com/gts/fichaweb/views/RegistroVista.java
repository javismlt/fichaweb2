package com.gts.fichaweb.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import repositorios.UsuarioRepositorio;
import repositorios.RegistroRepositorio;
import modelos.Usuario;
import modelos.Registro;
import java.time.*;
import com.vaadin.flow.component.select.Select;
import java.util.List;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.notification.Notification;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

@Route("registro")
public class RegistroVista extends AppLayout {
    private final UsuarioRepositorio usuarioRepositorio;
    private final RegistroRepositorio registroRepositorio;
    private Usuario usuarioActual;
    private Usuario usuarioAnterior;
    private Usuario usuarioActualAux;

    private Button boton1;
    private Button boton2;
    private Button boton3;
    private Button boton4;
    private Button boton5;

    public RegistroVista(UsuarioRepositorio usuarioRepositorio, RegistroRepositorio registroRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.registroRepositorio = registroRepositorio;
        String nombreUsuario = (String) VaadinSession.getCurrent().getAttribute("username");
        if (nombreUsuario == null) {
        	getElement().executeJs("window.location.href='/'");
        } else {
            this.usuarioActual = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
            crearHeader(nombreUsuario);
            cargarMenu();
        }
    }

    private void crearHeader(String nombreUsuario) {
        Anchor registro = new Anchor("registro", "Registro");
        registro.getElement().setAttribute("href", "/registro");
        registro.getStyle().set("color", "black").set("text-decoration", "none").set("font-size", "16px");
        
        Anchor consulta = new Anchor("consulta", "Consulta");
        consulta.getElement().setAttribute("href", "/consulta");
        consulta.getStyle().set("color", "black").set("text-decoration", "none").set("font-size", "16px");

        HorizontalLayout menuIzquierdo = new HorizontalLayout(registro, consulta);
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

    private void cargarMenu() {
        Image entradaImagen = new Image("img/entrada.png", "Entrada Icono");
        Image salidaImagen = new Image("img/salida.png", "Salida Icono");
        Image pausaImagen = new Image("img/pausa.png", "Pausa Icono");
        Image reanudarImagen = new Image("img/play.png", "Reanudar Icono");
        Image olvidadoImagen = new Image("img/olvidado.png", "Olvidado Icono");

        Image[] imagenes = {entradaImagen, salidaImagen, pausaImagen, reanudarImagen, olvidadoImagen};
        for (Image imagen : imagenes) {
            imagen.setWidth("30px");
            imagen.setHeight("30px");
            imagen.getStyle().set("margin-top", "8px");
        }

        boton1 = new Button("ENTRADA", entradaImagen);
        boton2 = new Button("PAUSA", pausaImagen);
        boton3 = new Button("REANUDAR", reanudarImagen);
        boton4 = new Button("SALIDA", salidaImagen);
        boton5 = new Button("FICHAJE OLVIDADO", olvidadoImagen);

        Button[] botones = {boton1, boton2, boton3, boton4, boton5};
        for (Button boton : botones) {
            boton.getStyle().set("width", "220px").set("height", "70px");
            boton.getElement().getStyle().set("display", "flex").set("align-items", "center").set("justify-content", "center");
        }

        if (usuarioActual.getRol() == 3 && usuarioActualAux == null) {
            aplicarEstiloBoton(boton1, false);
            aplicarEstiloBoton(boton2, false);
            aplicarEstiloBoton(boton3, false);
            aplicarEstiloBoton(boton4, false);
            aplicarEstiloBoton(boton5, false);
        } else {
            Usuario usuarioParaEvaluar = (usuarioActualAux != null) ? usuarioActualAux : usuarioActual;
            Registro ultimoRegistro = registroRepositorio.findTopByUsuarioOrderByIdDesc(usuarioParaEvaluar);
            String accionUltimoRegistro = ultimoRegistro != null ? ultimoRegistro.getAccion() : null;

            if ("SALIDA".equals(accionUltimoRegistro)) {
                aplicarEstiloBoton(boton1, true);   
                aplicarEstiloBoton(boton2, false);  
                aplicarEstiloBoton(boton3, false);  
                aplicarEstiloBoton(boton4, false);  
                aplicarEstiloBoton(boton5, true);   
            } else if ("ENTRADA".equals(accionUltimoRegistro) || ("RETORNO".equals(accionUltimoRegistro))) { 
                aplicarEstiloBoton(boton1, false);   
                aplicarEstiloBoton(boton2, true);  
                aplicarEstiloBoton(boton3, false);  
                aplicarEstiloBoton(boton4, true);  
                aplicarEstiloBoton(boton5, true);   
            } else if ("PAUSA".equals(accionUltimoRegistro)) { 
                aplicarEstiloBoton(boton1, false);   
                aplicarEstiloBoton(boton2, false); 
                aplicarEstiloBoton(boton3, true);  
                aplicarEstiloBoton(boton4, false); 
                aplicarEstiloBoton(boton5, true); 
            } else {
                aplicarEstiloBoton(boton1, true);   
                aplicarEstiloBoton(boton2, false);  
                aplicarEstiloBoton(boton3, false);  
                aplicarEstiloBoton(boton4, false);  
                aplicarEstiloBoton(boton5, true);
            }
        }
        
        usuarioAnterior = usuarioActual;
        if(usuarioActualAux != null) {
        	usuarioActual = usuarioActualAux;
        }
        
        if(usuarioActual.getFichajeManual() == 0) {
            boton5.setEnabled(false);
            boton5.setVisible(false);
        }
        
        boton1.addClickListener(event -> {
            registrarEntrada();
        });

        boton2.addClickListener(event -> {
            registrarPausa();
        });

        boton3.addClickListener(event -> {
            registrarReanudacion();
        });

        boton4.addClickListener(event -> {
            registrarSalida();
        });
        
        boton5.addClickListener(event -> {
        	UI.getCurrent().navigate("olvidado");
        });

        VerticalLayout layout = new VerticalLayout();

        Span nombreUser = new Span("Usuario: " + usuarioActual.getLoginUsuario());
        nombreUser.getStyle().set("font-size", "16px");

        Span nombreEmpresa = new Span("Empresa: " + usuarioActual.getEmpresa().getNombreComercial());
        nombreEmpresa.getStyle().set("font-size", "16px").set("margin-bottom", "15px");

        if (usuarioActual.getRol() == 3) {
            Select<Usuario> selectUsuarios = new Select<>();

            List<Usuario> usuariosDeLaEmpresa = usuarioRepositorio.findByEmpresaId(usuarioActual.getEmpresa().getId());
            usuariosDeLaEmpresa = usuariosDeLaEmpresa.stream().filter(usuario -> usuario.getRol() != 3 && usuario.getActivo() != 0).collect(Collectors.toList());

            selectUsuarios.setItems(usuariosDeLaEmpresa);
            selectUsuarios.setItemLabelGenerator(Usuario::getNombre);  
            selectUsuarios.setPlaceholder("Usuarios");
            
            Dialog pinDialog = new Dialog();

            HorizontalLayout fila0 = new HorizontalLayout();
            TextField pinField = new TextField("");
            pinField.setPlaceholder("PIN");
            pinField.setMaxLength(4); 
            pinField.setWidth("100%");
            fila0.add(pinField);

            HorizontalLayout fila1 = new HorizontalLayout();
            fila1.setSpacing(true);
            Button botonNum1 = new Button("1");
            Button botonNum2 = new Button("2");
            Button botonNum3 = new Button("3");
            botonNum1.getStyle().set("cursor", "pointer");
            botonNum2.getStyle().set("cursor", "pointer");
            botonNum3.getStyle().set("cursor", "pointer");
            fila1.add(botonNum1, botonNum2, botonNum3);

            HorizontalLayout fila2 = new HorizontalLayout();
            fila2.setSpacing(true);
            Button botonNum4 = new Button("4");
            Button botonNum5 = new Button("5");
            Button botonNum6 = new Button("6");
            botonNum4.getStyle().set("cursor", "pointer");
            botonNum5.getStyle().set("cursor", "pointer");
            botonNum6.getStyle().set("cursor", "pointer");
            fila2.add(botonNum4, botonNum5, botonNum6);

            HorizontalLayout fila3 = new HorizontalLayout();
            fila3.setSpacing(true);
            Button botonNum7 = new Button("7");
            Button botonNum8 = new Button("8");
            Button botonNum9 = new Button("9");
            botonNum7.getStyle().set("cursor", "pointer");
            botonNum8.getStyle().set("cursor", "pointer");
            botonNum9.getStyle().set("cursor", "pointer");
            fila3.add(botonNum7, botonNum8, botonNum9);

            HorizontalLayout fila4 = new HorizontalLayout();
            fila4.setSpacing(true);
            Button botonLimpiar = new Button("CLEAR");
            botonLimpiar.getStyle().set("background-color", "red").set("color", "white").set("cursor", "pointer");
            Button botonNum0 = new Button("0");
            botonNum0.getStyle().set("cursor", "pointer");
            Button verificarPinButton = new Button("OK");
            verificarPinButton.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer");
            fila4.add(botonLimpiar, botonNum0, verificarPinButton);

            botonNum1.addClickListener(click -> agregarNumero(pinField, "1"));
            botonNum2.addClickListener(click -> agregarNumero(pinField, "2"));
            botonNum3.addClickListener(click -> agregarNumero(pinField, "3"));
            botonNum4.addClickListener(click -> agregarNumero(pinField, "4"));
            botonNum5.addClickListener(click -> agregarNumero(pinField, "5"));
            botonNum6.addClickListener(click -> agregarNumero(pinField, "6"));
            botonNum7.addClickListener(click -> agregarNumero(pinField, "7"));
            botonNum8.addClickListener(click -> agregarNumero(pinField, "8"));
            botonNum9.addClickListener(click -> agregarNumero(pinField, "9"));
            botonNum0.addClickListener(click -> agregarNumero(pinField, "0"));
            botonLimpiar.addClickListener(click -> pinField.clear());

            verificarPinButton.addClickListener(click -> {
                Usuario usuarioSeleccionado = selectUsuarios.getValue();
                if (usuarioSeleccionado != null) {
                    String pinIngresado = pinField.getValue();
                    if (usuarioSeleccionado.getPin().equals(pinIngresado)) {
                        pinDialog.close();
                        Notification.show("PIN correcto, acceso concedido", 2000, Notification.Position.TOP_CENTER);
                        usuarioActualAux = usuarioSeleccionado; 
                        VaadinSession.getCurrent().setAttribute("UserManual", usuarioSeleccionado.getLoginUsuario());
                        cargarMenu(); 
                    } else {
                        Notification.show("PIN incorrecto inténtalo de nuevo", 2000, Notification.Position.TOP_CENTER);
                        pinField.clear();
                    }
                }
            });

            pinDialog.add(fila0, fila1, fila2, fila3, fila4);

            selectUsuarios.addValueChangeListener(event -> {
                Usuario usuarioSeleccionado = event.getValue();
                if (usuarioSeleccionado != null) {
                    System.out.println("Usuario seleccionado: " + usuarioSeleccionado.getNombre());
                    pinField.setValue(""); 
                    pinDialog.open();
                }
            });
            layout.add(selectUsuarios);
        }

        layout.add(nombreUser, nombreEmpresa, boton1, boton2, boton3, boton4, boton5);
        layout.setAlignItems(Alignment.CENTER);
        layout.setJustifyContentMode(JustifyContentMode.CENTER);
        layout.setSizeFull();
        setContent(layout);
    }

    
    private void registrarEntrada() {
        Usuario usuario = obtenerUsuarioParaAccion();
        if (usuario != null) {
            Registro registroEntrada = new Registro();
            registroEntrada.setUsuarioId(usuario);  
            registroEntrada.setFechaRegistro(LocalDate.now());
            registroEntrada.setHora(LocalDateTime.now().toLocalTime());
            registroEntrada.setAccion("ENTRADA");
            registroEntrada.setObservaciones("Entrada de personal");
            registroEntrada.setValidado(1);
            registroEntrada.setOrigen(obtenerOrigen());

            registroRepositorio.save(registroEntrada);
            restaurarMultiusuario();
            cargarMenu(); 
        }
    }
    
    private void registrarSalida() {
        Usuario usuario = obtenerUsuarioParaAccion();
        Registro ultimoRegistro = registroRepositorio.findTopByUsuarioAndAccionOrderByIdDesc(usuario, "ENTRADA");
        Integer idUltimaEntrada = ultimoRegistro != null ? ultimoRegistro.getId() : null;
        if (usuario != null && idUltimaEntrada != null) {
            Registro registroSalida = new Registro();
            registroSalida.setUsuarioId(usuario);  
            registroSalida.setFechaRegistro(LocalDate.now());
            registroSalida.setHora(LocalDateTime.now().toLocalTime());
            registroSalida.setAccion("SALIDA");
            registroSalida.setObservaciones("Salida de personal");
            registroSalida.setValidado(1);
            registroSalida.setOrigen(obtenerOrigen());
            registroSalida.setIdAsociado(idUltimaEntrada);

            registroRepositorio.save(registroSalida);
            restaurarMultiusuario();
            cargarMenu(); 
        }
    }
    
    private void registrarPausa() {
        Usuario usuario = obtenerUsuarioParaAccion();
        Registro ultimoRegistro = registroRepositorio.findTopByUsuarioAndAccionOrderByIdDesc(usuario, "ENTRADA");
        Integer idUltimaEntrada = ultimoRegistro != null ? ultimoRegistro.getId() : null;
        if (usuario != null && idUltimaEntrada != null) {
            Registro registroPausa = new Registro();
            registroPausa.setUsuarioId(usuario);  
            registroPausa.setFechaRegistro(LocalDate.now());
            registroPausa.setHora(LocalDateTime.now().toLocalTime());
            registroPausa.setAccion("PAUSA");
            registroPausa.setObservaciones("Pausa de la jornada");
            registroPausa.setValidado(1);
            registroPausa.setOrigen(obtenerOrigen());
            registroPausa.setIdAsociado(idUltimaEntrada);

            registroRepositorio.save(registroPausa);
            restaurarMultiusuario();
            cargarMenu(); 
        }
    }

    private void registrarReanudacion() {
        Usuario usuario = obtenerUsuarioParaAccion();
        Registro ultimoRegistro = registroRepositorio.findTopByUsuarioAndAccionOrderByIdDesc(usuario, "PAUSA");
        Integer idUltimaPausa = ultimoRegistro != null ? ultimoRegistro.getId() : null;
        if (usuario != null && idUltimaPausa != null) {
        	Registro registroReanudacion = new Registro();
        	registroReanudacion.setUsuarioId(usuario);  
        	registroReanudacion.setFechaRegistro(LocalDate.now());
        	registroReanudacion.setHora(LocalDateTime.now().toLocalTime());
        	registroReanudacion.setAccion("RETORNO");
        	registroReanudacion.setObservaciones("Reanudación de la jornada");
        	registroReanudacion.setValidado(1);
        	registroReanudacion.setOrigen(obtenerOrigen());
        	registroReanudacion.setIdAsociado(idUltimaPausa);

            registroRepositorio.save(registroReanudacion);
            restaurarMultiusuario();
            cargarMenu(); 
        }
    }

    private void aplicarEstiloBoton(Button boton, boolean habilitado) {
        boton.setEnabled(habilitado);
        boton.getElement().getStyle().set("transition", "all 0.3s ease");

        if (habilitado) {
            boton.getElement().getStyle().set("background-color", "#0066cc");
            boton.getElement().getStyle().set("color", "white");
            boton.getElement().getStyle().set("cursor", "pointer");
            boton.getElement().executeJs(
                "this.addEventListener('mouseover', function() {" +
                "  this.style.backgroundColor = '#005bb5';" +
                "});" +
                "this.addEventListener('mouseout', function() {" +
                "  this.style.backgroundColor = '#0066cc';" +
                "});"
            );
        } else {
            boton.getElement().getStyle().set("background-color", "#ddd");
            boton.getElement().getStyle().set("color", "#999");
            boton.getElement().getStyle().set("cursor", "not-allowed");
            boton.getElement().executeJs("this.replaceWith(this.cloneNode(true));");
        }
    }
    
    private void restaurarMultiusuario() {
        if (usuarioAnterior != null && usuarioAnterior.getRol() == 3) {
            usuarioActual = usuarioAnterior;
            usuarioActualAux = null;
        }
    }
    
    private Usuario obtenerUsuarioParaAccion() {
        return (usuarioActualAux != null) ? usuarioActualAux : usuarioActual;
    }

    private void agregarNumero(TextField pinField, String numero) {
        String total = pinField.getValue();
        if (total.length() < 4) {
            pinField.setValue(total + numero);
        }
    }
    
    public String obtenerOrigen() {
        String os = System.getProperty("os.name").toLowerCase();  

        if (os.contains("win") || os.contains("mac") || os.contains("nux")) {
            return "W";
        } else {
        	return "M"; 
        }
    }
}