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
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.checkbox.Checkbox;

@Route("olvidado")
public class FichajeOlvidado extends AppLayout{

    private final UsuarioRepositorio usuarioRepositorio;
    private final RegistroRepositorio registroRepositorio;
    private Usuario usuarioActual;
    private Usuario usuarioActualAux;
    private String usuario1;

    public FichajeOlvidado(UsuarioRepositorio usuarioRepositorio, RegistroRepositorio registroRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.registroRepositorio = registroRepositorio;
        
        String nombreUsuario = (String) VaadinSession.getCurrent().getAttribute("username");
        if (nombreUsuario == null) {
            getElement().executeJs("window.location.href='/'");
        } else {
        	if (VaadinSession.getCurrent().getAttribute("UserManual") == null) {
        		usuarioActualAux = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
        		usuario1 = usuarioActualAux.getLoginUsuario();

        	} else {
        		usuario1 = (String) VaadinSession.getCurrent().getAttribute("UserManual");
        	}
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
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setAlignItems(Alignment.CENTER);

        DatePicker fechaPicker = new DatePicker("Fecha");

        TimePicker horaInicioPicker = new TimePicker("Hora de entrada");
        horaInicioPicker.setStep(Duration.ofMinutes(15));

        TimePicker horaFinPicker = new TimePicker("Hora de salida");
        horaFinPicker.setStep(Duration.ofMinutes(15));

        Checkbox incluirPausaCheckbox = new Checkbox("Incluir descanso");

        TimePicker horaPausaPicker = new TimePicker("Hora de pausa");
        horaPausaPicker.setStep(Duration.ofMinutes(15));
        horaPausaPicker.setVisible(false);

        TimePicker horaReanudacionPicker = new TimePicker("Hora de reanudación");
        horaReanudacionPicker.setStep(Duration.ofMinutes(15));
        horaReanudacionPicker.setVisible(false);

        incluirPausaCheckbox.addValueChangeListener(event -> {
            boolean visible = event.getValue();
            horaPausaPicker.setVisible(visible);
            horaReanudacionPicker.setVisible(visible);

            if (visible) {
                actualizarRangos(horaInicioPicker, horaFinPicker, horaPausaPicker, horaReanudacionPicker);
            }
        });

        horaInicioPicker.addValueChangeListener(event -> {
            if (incluirPausaCheckbox.getValue()) {
                actualizarRangos(horaInicioPicker, horaFinPicker, horaPausaPicker, horaReanudacionPicker);
            }
        });

        horaFinPicker.addValueChangeListener(event -> {
            if (incluirPausaCheckbox.getValue()) {
                actualizarRangos(horaInicioPicker, horaFinPicker, horaPausaPicker, horaReanudacionPicker);
            }
        });

        Button btnguardar = new Button("Fichar");
        btnguardar.setWidth("100px");
        btnguardar.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer");

        btnguardar.addClickListener(event -> {
            LocalDate fecha = fechaPicker.getValue();
            LocalTime horaInicio = horaInicioPicker.getValue();
            LocalTime horaFin = horaFinPicker.getValue();

            if (fecha == null || horaInicio == null || horaFin == null) {
                Notification.show("Por favor completa todos los campos", 2000, Notification.Position.TOP_CENTER);
                return;
            }

            if (horaInicio.isAfter(horaFin)) {
                Notification.show("La hora de entrada no puede ser después de la de salida", 2000, Notification.Position.TOP_CENTER);
                return;
            }

            registrarFichajeEntrada(usuario1, fecha, horaInicio, incluirPausaCheckbox.getValue());

            if (incluirPausaCheckbox.getValue()) {
                LocalTime horaPausa = horaPausaPicker.getValue();
                LocalTime horaReanudacion = horaReanudacionPicker.getValue();

                if (horaPausa == null || horaReanudacion == null) {
                    Notification.show("Completa los campos de pausa y reanudación", 2000, Notification.Position.TOP_CENTER);
                    return;
                }

                if (horaPausa.isAfter(horaReanudacion)) {
                    Notification.show("La hora de pausa no puede ser después de la reanudación", 2000, Notification.Position.TOP_CENTER);
                    return;
                }

                registrarPausa(usuario1, fecha, horaPausa);
                registrarReanudacion(usuario1, fecha, horaReanudacion);
            }

            registrarFichajeSalida(usuario1, fecha, horaFin);

            Notification.show("Registro guardado", 2000, Notification.Position.TOP_CENTER);

            fechaPicker.clear();
            horaInicioPicker.clear();
            horaFinPicker.clear();
            horaPausaPicker.clear();
            horaReanudacionPicker.clear();
            incluirPausaCheckbox.clear();
        });

        layout.add(fechaPicker, horaInicioPicker, horaFinPicker, incluirPausaCheckbox, horaPausaPicker, horaReanudacionPicker, btnguardar);
        setContent(layout);
    }


    private void actualizarRangos(TimePicker horaInicioPicker, TimePicker horaFinPicker, TimePicker horaPausaPicker, TimePicker horaReanudacionPicker) {
        LocalTime horaInicio = horaInicioPicker.getValue();
        LocalTime horaFin = horaFinPicker.getValue();

        if (horaInicio != null && horaFin != null) {
            horaPausaPicker.setMin(horaInicio);
            horaPausaPicker.setMax(horaFin);

            LocalTime horaPausa = horaPausaPicker.getValue();
            LocalTime minReanudacion = (horaPausa != null) ? horaPausa : horaInicio;

            horaReanudacionPicker.setMin(minReanudacion);
            horaReanudacionPicker.setMax(horaFin);

            horaPausaPicker.addValueChangeListener(event -> {
                LocalTime nuevaHoraPausa = event.getValue();
                if (nuevaHoraPausa != null) {
                    horaReanudacionPicker.setMin(nuevaHoraPausa);
                } else {
                    horaReanudacionPicker.setMin(horaInicio);
                }
                horaReanudacionPicker.setMax(horaFin);
            });
        }
    }

    private void registrarFichajeEntrada (String nombreUsuario, LocalDate fecha, LocalTime horaInicio, boolean incluirPausa) {
        Usuario usuario = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
        if (usuario != null) {
            Registro registro = new Registro();
            registro.setUsuarioId(usuario);  
            registro.setFechaRegistro(fecha);
            registro.setHora(horaInicio);
            registro.setAccion("ENTRADA");
            registro.setObservaciones("Entrada fichaje manual");
            registro.setOrigen("N");
            registro.setValidado(0);
            
            registroRepositorio.save(registro);
            cargarMenu(); 
        }
    }
    
    private void registrarFichajeSalida (String nombreUsuario, LocalDate fecha, LocalTime horaFin) {
        Usuario usuario = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
        Registro ultimoRegistro = registroRepositorio.findTopByUsuarioAndAccionOrderByIdDesc(usuario, "ENTRADA");
        Integer idUltimaEntrada = ultimoRegistro != null ? ultimoRegistro.getId() : null;
        if (usuario != null) {
            Registro registro = new Registro();
            registro.setUsuarioId(usuario);  
            registro.setFechaRegistro(fecha);
            registro.setHora(horaFin);
            registro.setAccion("SALIDA");
            registro.setObservaciones("Salida fichaje manual");
            registro.setOrigen("N");
            registro.setValidado(0);
            registro.setIdAsociado(idUltimaEntrada);
            
            registroRepositorio.save(registro);
            cargarMenu(); 
        }
    }
    
    private void registrarPausa(String nombreUsuario, LocalDate fecha, LocalTime horaPausa) {
    	Usuario usuario = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
    	Registro ultimoRegistro = registroRepositorio.findTopByUsuarioAndAccionOrderByIdDesc(usuario, "ENTRADA");
        Integer idUltimaEntrada = ultimoRegistro != null ? ultimoRegistro.getId() : null;
    	if (idUltimaEntrada != null) {
            Registro registro = new Registro();
            registro.setUsuarioId(usuario);  
            registro.setFechaRegistro(fecha);
            registro.setHora(horaPausa);
            registro.setAccion("PAUSA");
            registro.setObservaciones("Pausa fichaje manual");
            registro.setOrigen("N");
            registro.setValidado(0);
            registro.setIdAsociado(idUltimaEntrada);
     
            registroRepositorio.save(registro);
            cargarMenu(); 
        }
    }
    
    private void registrarReanudacion(String nombreUsuario, LocalDate fecha, LocalTime horaReanudacion) {
    	Usuario usuario = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
    	Registro ultimoRegistro = registroRepositorio.findTopByUsuarioAndAccionOrderByIdDesc(usuario, "PAUSA");
        Integer idUltimaPausa = ultimoRegistro != null ? ultimoRegistro.getId() : null;
    	if (idUltimaPausa != null) {
            Registro registro = new Registro();
            registro.setUsuarioId(usuario);  
            registro.setFechaRegistro(fecha);
            registro.setHora(horaReanudacion);
            registro.setAccion("REANUDACION");
            registro.setObservaciones("Reanudación fichaje manual");
            registro.setOrigen("N");
            registro.setValidado(0);
            registro.setIdAsociado(idUltimaPausa);
     
            registroRepositorio.save(registro);
            cargarMenu(); 
        }
    }
}
