package com.gts.fichaweb.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import java.time.format.DateTimeFormatter;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.applayout.AppLayout;
import modelos.Usuario;
import modelos.Registro;
import repositorios.RegistroRepositorio;
import repositorios.UsuarioRepositorio;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import java.time.LocalDate;
import com.vaadin.flow.component.grid.Grid;
import java.util.List;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.vaadin.flow.component.notification.Notification;
import java.io.ByteArrayOutputStream;
import com.itextpdf.layout.element.Paragraph;
import java.util.stream.Collectors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import java.io.IOException;
import java.time.LocalTime;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.dialog.Dialog;
import java.time.Duration;
import org.apache.poi.ss.util.CellRangeAddress;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import com.vaadin.flow.component.dependency.CssImport;
import modelos.Logs_modificaciones;
import repositorios.Logs_modificacionesRepositorio;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;

@Route("inspector")
@CssImport(value = "./themes/my-theme/styles.css", themeFor = "vaadin-grid") 
public class InspectorVista extends AppLayout {
    private final UsuarioRepositorio usuarioRepositorio;
    private final RegistroRepositorio registroRepositorio; 
    private final Logs_modificacionesRepositorio logs_modificacionesRepositorio;  
    private Usuario usuarioActual;
    private Grid<Registro> grid;
    private Span totalHorasTrabajadasLabel;
    private DatePicker fechaInicio;
    private DatePicker fechaFin;
    private Usuario usuarioActualAux;

    public InspectorVista(UsuarioRepositorio usuarioRepositorio, RegistroRepositorio registroRepositorio, Logs_modificacionesRepositorio logs_modificacionesRepositorio) {
        this.registroRepositorio = registroRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.logs_modificacionesRepositorio = logs_modificacionesRepositorio;
        
        String nombreUsuario = (String) VaadinSession.getCurrent().getAttribute("username");
        if (nombreUsuario == null) {
            getElement().executeJs("window.location.href='/'");
        } else {
            this.usuarioActual = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
            if (usuarioActual.getRol() == 5) {
            	crearHeader(nombreUsuario);
            	crearContenido();
            } else {
            	Notification.show("Usuario no inspector", 2000, Notification.Position.TOP_CENTER);
                getElement().executeJs("setTimeout(() => window.location.href='/', 2000)");
                return;
            }
        }
    }

    private void crearHeader(String nombreUsuario) {
        Anchor consulta = new Anchor("inspector", "Consulta");
        consulta.getElement().setAttribute("href", "/inspector");
        consulta.getStyle().set("color", "black").set("text-decoration", "none").set("font-size", "16px");

        HorizontalLayout menuIzquierdo = new HorizontalLayout(consulta);
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

    private void crearContenido() {
        fechaInicio = new DatePicker("Fecha inicio");
        fechaFin = new DatePicker("Fecha fin");
        fechaFin.setValue(LocalDate.now());

        fechaInicio.setLocale(new java.util.Locale("es", "ES"));
        fechaFin.setLocale(new java.util.Locale("es", "ES"));

        Select<Usuario> selectUsuarios = new Select<>();
        selectUsuarios.setLabel("Usuarios");
    
        HorizontalLayout fechasLayout = new HorizontalLayout(selectUsuarios, fechaInicio, fechaFin);
        fechasLayout.setAlignItems(Alignment.CENTER);
        fechasLayout.addClassName("fechas-layout");
        fechasLayout.setWidthFull();
        
	    List<Usuario> usuariosDeLaEmpresa = new ArrayList<>();

	    usuariosDeLaEmpresa = usuarioRepositorio.findByEmpresaId(usuarioActual.getEmpresa().getId()).stream().filter(usuario -> usuario.getRol() == 4 && usuario.getActivo() == 1).collect(Collectors.toList());
	
	    Usuario general = new Usuario();
	    general.setNombre("General");
	    general.setLoginUsuario("General"); 
	    general.setId(-1);
	    usuariosDeLaEmpresa.add(0, general); 
	    
	    selectUsuarios.setItems(usuariosDeLaEmpresa);
	    selectUsuarios.setItemLabelGenerator(Usuario::getLoginUsuario);
	    selectUsuarios.setPlaceholder("Seleccione un usuario");
	
	    selectUsuarios.addValueChangeListener(event -> {
	        Usuario usuarioSeleccionado = event.getValue();
	        if (usuarioSeleccionado != null && usuarioSeleccionado.getId() == -1) { 
	        	usuarioActualAux = usuarioSeleccionado;
	            actualizarGrid(fechaInicio.getValue(), fechaFin.getValue());
	        } else if (usuarioSeleccionado != null) {
	            usuarioActualAux = usuarioSeleccionado;
                actualizarGrid(fechaInicio.getValue(), fechaFin.getValue());
	        }
	    });
	
	    grid = new Grid<>(Registro.class); 
        grid.setHeightFull();
        grid.removeAllColumns();
        grid.addClassName("responsive-grid");
        
        grid.addColumn(registro -> registro.getFechaRegistro().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).setHeader("Fecha").setAutoWidth(true);
        grid.addColumn(registro -> registro.getAccion()).setHeader("Acción").setAutoWidth(true);
        grid.addColumn(registro -> {LocalTime hora = registro.getHora();return hora != null ? hora.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "";}).setHeader("Hora");
        grid.addColumn(new ComponentRenderer<>(registro -> {
            Span span = new Span();
            if (Integer.valueOf(1).equals(registro.getValidado())) {
                span.add("🟢");
            } else if (Integer.valueOf(0).equals(registro.getValidado())) {
            	span.add("🔴");
            }
            return span;
        })).setHeader("Validado").setAutoWidth(true);
        Grid.Column<Registro> usuarioColumn = grid.addColumn(registro -> registro.getUsuarioId().getLoginUsuario()).setHeader("Usuario").setAutoWidth(true);
        grid.addColumn(Registro::getObservaciones).setHeader("Observaciones");
        usuarioColumn.setVisible(usuarioActualAux != null && usuarioActualAux.getId() == -1);
        
        selectUsuarios.addValueChangeListener(event -> {
            Usuario usuarioSeleccionado = event.getValue();
            if (usuarioSeleccionado != null) {
                usuarioActualAux = usuarioSeleccionado;
                usuarioColumn.setVisible(usuarioActualAux.getId() == -1);
                actualizarGrid(fechaInicio.getValue(), fechaFin.getValue());
            }
        });
        
        fechaInicio.addValueChangeListener(event -> actualizarGrid(fechaInicio.getValue(), fechaFin.getValue()));
        fechaFin.addValueChangeListener(event -> actualizarGrid(fechaInicio.getValue(), fechaFin.getValue()));
        
        totalHorasTrabajadasLabel = new Span("");
        totalHorasTrabajadasLabel.getStyle().set("font-weight", "bold").set("font-size", "16px");
        totalHorasTrabajadasLabel.addClassName("horas-trabajadas-label");
       
        Button menuPdf = new Button("Descarga PDF ▼");
        menuPdf.getStyle().set("margin-top", "37px").set("color", "white").set("background-color", "red").set("cursor", "pointer");
        
	        ContextMenu botonesPdf = new ContextMenu(menuPdf);
	        botonesPdf.setOpenOnClick(true);

	        MenuItem registrosPdf = botonesPdf.addItem("Registros PDF", e -> {
	            generarPdf(grid);
	        });
	        registrosPdf.getElement().getStyle().set("color", "red").set("background-color", "white").set("cursor", "pointer");
	
	        MenuItem logsPdf = botonesPdf.addItem("Logs PDF", e -> {
	            generarLogPdf();
	        });
	        logsPdf.getElement().getStyle().set("color", "red").set("background-color", "white").set("cursor", "pointer");
        
        Button menuExcel = new Button("Descarga EXCEL ▼");
        menuExcel.getStyle().set("margin-top", "37px").set("color", "white").set("background-color", "green").set("cursor", "pointer");
        
	        ContextMenu botonesExcel = new ContextMenu(menuExcel);
	        botonesExcel.setOpenOnClick(true);
	
	        MenuItem registrosExcel = botonesExcel.addItem("Registros EXCEL", e -> {
	            generarExcel(grid);
	        });
	        registrosExcel.getElement().getStyle().set("color", "green").set("background-color", "white").set("cursor", "pointer");
	
	        MenuItem logsExcel = botonesExcel.addItem("Logs EXCEL", e -> {
	            generarLogExcel();
	        });
	        logsExcel.getElement().getStyle().set("color", "green").set("background-color", "white").set("cursor", "pointer");
        
        fechasLayout.add(menuPdf, menuExcel);

        VerticalLayout contenidoPrincipal = new VerticalLayout(fechasLayout, totalHorasTrabajadasLabel, grid); 
        contenidoPrincipal.setPadding(true);
        contenidoPrincipal.setAlignItems(Alignment.START);
        contenidoPrincipal.setWidthFull();
        contenidoPrincipal.setHeightFull();
        
        setContent(contenidoPrincipal);
        actualizarGrid(fechaInicio.getValue(), fechaFin.getValue());
    }


    private void actualizarGrid(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Registro> registros;
        LocalDate hoy = LocalDate.now();
        
        if (usuarioActualAux == null) {
            registros = Collections.emptyList();
        } else if (usuarioActualAux.getId() == -1) { 
            if (fechaInicio != null && fechaFin != null) {
                registros = registroRepositorio.findByFechaRegistroBetweenAndActivo(fechaInicio, fechaFin, 1).stream().filter(registro -> registro.getUsuarioId().getEmpresa().getId().equals(usuarioActual.getEmpresa().getId())).collect(Collectors.toList());
            } else {
                registros = registroRepositorio.findByFechaRegistroAndActivo(hoy, 1).stream().filter(registro -> registro.getUsuarioId().getEmpresa().getId().equals(usuarioActual.getEmpresa().getId())).collect(Collectors.toList());
            }
        } else if (fechaInicio != null && fechaFin != null) {
            registros = registroRepositorio.findByFechaRegistroBetweenAndUsuario_IdAndActivo(fechaInicio, fechaFin, usuarioActualAux.getId().intValue(), 1);
        } else {
            registros = registroRepositorio.findByFechaRegistroAndUsuario_IdAndActivo(hoy, usuarioActualAux.getId().intValue(), 1);
        }
        
        registros.sort(
        	    Comparator.comparing(Registro::getFechaRegistro, Comparator.reverseOrder())
        	              .thenComparing(Registro::getHora, Comparator.reverseOrder())
        );
        grid.setItems(registros);
        
        String horasTrabajadas = calcularHorasTrabajadas(registros);
        totalHorasTrabajadasLabel.setText("TRABAJADO: " + horasTrabajadas + " horas");
    }
    
    private String calcularHorasTrabajadas(List<Registro> registros) {
        registros.sort((r1, r2) -> {
            int cmpFecha = r1.getFechaRegistro().compareTo(r2.getFechaRegistro());
            if (cmpFecha != 0) return cmpFecha;
            return r1.getHora().compareTo(r2.getHora());
        });

        Duration totalTrabajado = Duration.ZERO;
        Duration totalDescanso = Duration.ZERO;

        LocalTime inicioIntervalo = null;  
        LocalTime inicioDescanso = null; 

        for (Registro reg : registros) {
            String accion = reg.getAccion().toLowerCase();
            LocalTime hora = reg.getHora();

            if (hora == null) continue;  

            switch (accion) {
                case "entrada":
                case "reanudacion":
                    if (inicioDescanso != null) {
                        Duration descanso = Duration.between(inicioDescanso, hora);
                        totalDescanso = totalDescanso.plus(descanso);
                        inicioDescanso = null;
                    }
                    if (inicioIntervalo == null) {
                        inicioIntervalo = hora;
                    }
                    break;

                case "pausa":
                    if (inicioIntervalo != null) {
                        Duration trabajado = Duration.between(inicioIntervalo, hora);
                        totalTrabajado = totalTrabajado.plus(trabajado);
                        inicioIntervalo = null;
                    }
                    inicioDescanso = hora;
                    break;

                case "salida":
                    if (inicioIntervalo != null) {
                        Duration trabajado = Duration.between(inicioIntervalo, hora);
                        totalTrabajado = totalTrabajado.plus(trabajado);
                        inicioIntervalo = null;
                    }
                    if (inicioDescanso != null) {
                        Duration descanso = Duration.between(inicioDescanso, hora);
                        totalDescanso = totalDescanso.plus(descanso);
                        inicioDescanso = null;
                    }
                    break;
            }
        }

        registros.sort((r1, r2) -> {
            int cmpFecha = r2.getFechaRegistro().compareTo(r1.getFechaRegistro());
            if (cmpFecha != 0) return cmpFecha;
            return r2.getHora().compareTo(r1.getHora());
        });
        
        long horas = totalTrabajado.toHours();
        long minutos = totalTrabajado.toMinutes() % 60;
        return String.format("%02d:%02d", horas, minutos);
    }

    private void generarPdf(Grid<Registro> grid) {
        List<Registro> registros = grid.getListDataView().getItems().collect(Collectors.toList());

        if (registros.isEmpty()) {
            Notification.show("No hay registros para generar el PDF", 2000, Notification.Position.TOP_CENTER);
            return;
        }
        
        String horasTrabajadas = calcularHorasTrabajadas(registros);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        Paragraph titulo = new Paragraph("FichaWeb").setFontSize(28) .setBold(); 
        document.add(titulo);
        
        Paragraph titulo2 = new Paragraph("Registros de Jornada Laboral").setFontSize(14);
        document.add(titulo2);
        
        if(usuarioActualAux.getId() == -1) {
        	Paragraph usuarioParrafo = new Paragraph("Usuario: " + usuarioActual.getNombre() + " (" + usuarioActual.getEmpresa().getNombreComercial() + ")").setFontSize(12);
        	document.add(usuarioParrafo);
        } else {
        	Paragraph usuarioParrafo = new Paragraph("Usuario: " + usuarioActualAux.getNombre() + " (" + usuarioActualAux.getEmpresa().getNombreComercial() + ")").setFontSize(12);
        	document.add(usuarioParrafo);
        }

        LocalDate fechaInicioValor = fechaInicio.getValue() != null ? fechaInicio.getValue() : LocalDate.now();
        LocalDate fechaFinValor = fechaFin.getValue() != null ? fechaFin.getValue() : LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaInicioFormateada = fechaInicioValor.format(formatter);
        String fechaFinFormateada = fechaFinValor.format(formatter);

        String rangoFechas;
        if (fechaInicioValor.equals(fechaFinValor)) {
            rangoFechas = "Fecha: " + fechaInicioFormateada;
        } else {
            rangoFechas = "Fecha: " + fechaInicioFormateada + " - " + fechaFinFormateada;
        }

        Paragraph fechasParrafo = new Paragraph(rangoFechas).setFontSize(12);
        document.add(fechasParrafo);

        Paragraph horasParrafo = new Paragraph("Total trabajado: " + horasTrabajadas + " horas").setFontSize(12).setMarginBottom(15);
        document.add(horasParrafo);
        
        Table table = new Table(6);
        
        table.addHeaderCell(new Cell().add(new Paragraph("FECHA")));
        table.addHeaderCell(new Cell().add(new Paragraph("ACCION")));
        table.addHeaderCell(new Cell().add(new Paragraph("HORA")));
        table.addHeaderCell(new Cell().add(new Paragraph("ORIGEN")));
        if(usuarioActualAux.getId() == -1) {
        	table.addHeaderCell(new Cell().add(new Paragraph("USUARIO")));
        } else {
        	table.addHeaderCell(new Cell().add(new Paragraph("ESTADO")));
        }
        table.addHeaderCell(new Cell().add(new Paragraph("OBSERVACIONES")));

        for (Registro registro : registros) {
        	table.addCell(new Cell().add(new Paragraph(registro.getFechaRegistro().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))));
            table.addCell(new Cell().add(new Paragraph(registro.getAccion())));
            table.addCell(new Cell().add(new Paragraph(registro.getHora() != null ? registro.getHora().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "")));
            table.addCell(new Cell().add(new Paragraph(registro.getOrigen())));
            if(usuarioActualAux.getId() == -1) {
            	table.addCell(new Cell().add(new Paragraph(registro.getUsuarioId().getLoginUsuario())));
            } else {
            	table.addCell(new Cell().add(new Paragraph(registro.getValidado() == 1 ? "VALIDADO" : "NO VALIDADO")));
            }
            table.addCell(new Cell().add(new Paragraph(registro.getObservaciones())));
        }

        document.add(table);
        document.close();

        byte[] pdfBytes = byteArrayOutputStream.toByteArray();
        String base64Pdf = java.util.Base64.getEncoder().encodeToString(pdfBytes);

        String nombreArchivo;
        if (fechaInicioValor.equals(fechaFinValor)) {
        	if(usuarioActualAux.getId() == -1) {
        		nombreArchivo = "FICHAJES_GENERAL_" + fechaInicioFormateada + ".pdf";
            } else {
            	nombreArchivo = "FICHAJES_" + usuarioActualAux.getNombre() + "_" + fechaInicioFormateada + ".pdf";
            }
        } else {
        	if(usuarioActualAux.getId() == -1) {
        		nombreArchivo = "FICHAJES_GENERAL_" + fechaInicioFormateada + "_" + fechaFinFormateada + ".pdf";
            } else {
            	nombreArchivo = "FICHAJES_" + usuarioActualAux.getNombre() + "_" + fechaInicioFormateada + "_" + fechaFinFormateada + ".pdf";
            }
        }

        UI.getCurrent().getPage().executeJs(
            "var link = document.createElement('a');" +
            "link.href = 'data:application/pdf;base64,' + $0;" +
            "link.download = '" + nombreArchivo + "';" +
            "link.click();", base64Pdf);
    }
    
    private void generarLogPdf() {
    	LocalDate fechaInicioValor = fechaInicio.getValue() != null ? fechaInicio.getValue() : LocalDate.now();
        LocalDate fechaFinValor = fechaFin.getValue() != null ? fechaFin.getValue() : LocalDate.now();

        List<Logs_modificaciones> logs = logs_modificacionesRepositorio.findByFechaBetween(fechaInicioValor, fechaFinValor);
        List<Logs_modificaciones> logsFiltrados = logs.stream().filter(log -> log.getRegistro() != null).filter(log -> {
                if (usuarioActualAux.getId() == -1) {
                    return log.getRegistro().getUsuarioId().getEmpresa().getId().equals(usuarioActual.getEmpresa().getId());
                } else {
                    return log.getRegistro().getUsuarioId().getId().equals(usuarioActualAux.getId());
                }
            }).collect(Collectors.toList());

        if (logsFiltrados.isEmpty()) {
            Notification.show("No hay registros para generar el PDF", 2000, Notification.Position.TOP_CENTER);
            return;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        Paragraph titulo = new Paragraph("FichaWeb").setFontSize(28) .setBold(); 
        document.add(titulo);
        
        Paragraph titulo2 = new Paragraph("Registros de Logs").setFontSize(14);
        document.add(titulo2);

        if(usuarioActualAux.getId() == -1) {
        	Paragraph usuarioParrafo = new Paragraph("Usuario: " + usuarioActual.getNombre() + " (" + usuarioActual.getEmpresa().getNombreComercial() + ")").setFontSize(12);
        	document.add(usuarioParrafo);
        } else {
        	Paragraph usuarioParrafo = new Paragraph("Usuario: " + usuarioActualAux.getNombre() + " (" + usuarioActualAux.getEmpresa().getNombreComercial() + ")").setFontSize(12);
        	document.add(usuarioParrafo);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaInicioFormateada = fechaInicioValor.format(formatter);
        String fechaFinFormateada = fechaFinValor.format(formatter);

        String rangoFechas;
        if (fechaInicioValor.equals(fechaFinValor)) {
            rangoFechas = "Fecha: " + fechaInicioFormateada;
        } else {
            rangoFechas = "Fecha: " + fechaInicioFormateada + " - " + fechaFinFormateada;
        }

        Paragraph fechasParrafo = new Paragraph(rangoFechas).setFontSize(12);
        document.add(fechasParrafo);
        
        Table table;
        
        if(usuarioActualAux.getId() == -1) {
        	table = new Table(5);
        } else {
        	table = new Table(4);
        }
        
        table.addHeaderCell(new Cell().add(new Paragraph("FECHA")));
        table.addHeaderCell(new Cell().add(new Paragraph("MODIFICADO")));
        table.addHeaderCell(new Cell().add(new Paragraph("VALOR PREVIO")));
        table.addHeaderCell(new Cell().add(new Paragraph("VALOR NUEVO")));
        if(usuarioActualAux.getId() == -1) {
        	table.addHeaderCell(new Cell().add(new Paragraph("USUARIO")));
        }

        for (Logs_modificaciones log : logsFiltrados) {
        	table.addCell(new Cell().add(new Paragraph(log.getFecha().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))));
            table.addCell(new Cell().add(new Paragraph(log.getCampo())));
            table.addCell(new Cell().add(new Paragraph(log.getValorPrevio())));
            table.addCell(new Cell().add(new Paragraph(log.getValorNuevo())));
            if(usuarioActualAux.getId() == -1) {
            	table.addCell(new Cell().add(new Paragraph(log.getRegistro().getUsuarioId().getLoginUsuario())));
            }
        }

        document.add(table);
        document.close();

        byte[] pdfBytes = byteArrayOutputStream.toByteArray();
        String base64Pdf = java.util.Base64.getEncoder().encodeToString(pdfBytes);

        String nombreArchivo;
        if (fechaInicioValor.equals(fechaFinValor)) {
        	if(usuarioActualAux.getId() == -1) {
        		nombreArchivo = "LOGS_GENERAL_" + fechaInicioFormateada + ".pdf";
            } else {
            	nombreArchivo = "LOGS_" + usuarioActualAux.getNombre() + "_" + fechaInicioFormateada + ".pdf";
            }
        } else {
        	if(usuarioActualAux.getId() == -1) {
        		nombreArchivo = "LOGS_GENERAL_" + fechaInicioFormateada + "_" + fechaFinFormateada + ".pdf";
            } else {
            	nombreArchivo = "LOGS_" + usuarioActualAux.getNombre() + "_" + fechaInicioFormateada + "_" + fechaFinFormateada + ".pdf";
            }
        }

        UI.getCurrent().getPage().executeJs(
            "var link = document.createElement('a');" +
            "link.href = 'data:application/pdf;base64,' + $0;" +
            "link.download = '" + nombreArchivo + "';" +
            "link.click();", base64Pdf);
    }

    private void generarExcel(Grid<Registro> grid) {
        List<Registro> registros = grid.getListDataView().getItems().collect(Collectors.toList());

        if (registros.isEmpty()) {
        	Notification.show("No hay registros para generar el EXCEL", 2000, Notification.Position.TOP_CENTER);
            return;
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Registros");

        XSSFRow titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("FichaWeb");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        XSSFRow userRow = sheet.createRow(1);
        if(usuarioActualAux.getId() == -1) {
        	userRow.createCell(0).setCellValue("Usuario: " + usuarioActual.getNombre() + " (" + usuarioActual.getEmpresa().getNombreComercial() + ")");
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));
        } else {
        	userRow.createCell(0).setCellValue("Usuario: " + usuarioActualAux.getNombre() + " (" + usuarioActualAux.getEmpresa().getNombreComercial() + ")");
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));
        }

        LocalDate fechaInicioValor = fechaInicio.getValue() != null ? fechaInicio.getValue() : LocalDate.now();
        LocalDate fechaFinValor = fechaFin.getValue() != null ? fechaFin.getValue() : LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaInicioFormateada = fechaInicioValor.format(formatter);
        String fechaFinFormateada = fechaFinValor.format(formatter);

        String rangoFechas;
        if (fechaInicioValor.equals(fechaFinValor)) {
            rangoFechas = "Fecha: " + fechaInicioFormateada;
        } else {
            rangoFechas = "Fecha: " + fechaInicioFormateada + " - " + fechaFinFormateada;
        }

        XSSFRow dateRow = sheet.createRow(2);
        dateRow.createCell(0).setCellValue(rangoFechas);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 4));

        String horasTrabajadas = calcularHorasTrabajadas(registros);
        XSSFRow hoursRow = sheet.createRow(3);
        hoursRow.createCell(0).setCellValue("Total trabajado: " + horasTrabajadas + " horas");
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 4));
  
        XSSFRow headerRow = sheet.createRow(4);
        headerRow.createCell(0).setCellValue("FECHA");
        headerRow.createCell(1).setCellValue("ACCION");
        headerRow.createCell(2).setCellValue("HORA");
        headerRow.createCell(3).setCellValue("ORIGEN");
        if (usuarioActualAux.getId() == -1) {
            headerRow.createCell(4).setCellValue("USUARIO");
            headerRow.createCell(5).setCellValue("OBSERVACIONES");
        } else {
            headerRow.createCell(4).setCellValue("ESTADO");
            headerRow.createCell(5).setCellValue("OBSERVACIONES");
        }

        int rowIndex = 5;
        for (Registro registro : registros) {
            XSSFRow row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(registro.getFechaRegistro().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            row.createCell(1).setCellValue(registro.getAccion());
            row.createCell(2).setCellValue(registro.getHora() != null ? registro.getHora().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "");
            row.createCell(3).setCellValue(registro.getOrigen());
            if (usuarioActualAux.getId() == -1) {
                row.createCell(4).setCellValue(registro.getUsuarioId().getLoginUsuario());
                row.createCell(5).setCellValue(registro.getObservaciones());
            } else {
                row.createCell(4).setCellValue(registro.getValidado() == 1 ? "VALIDADO" : "NO VALIDADO");
                row.createCell(5).setCellValue(registro.getObservaciones());
            }
        }

        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }

        String nombreArchivo;
        if (fechaInicioValor.equals(fechaFinValor)) {
        	if(usuarioActualAux.getId() == -1) {
        		nombreArchivo = "FICHAJES_GENERAL_" + fechaInicioFormateada + ".xlsx";
            } else {
            	nombreArchivo = "FICHAJES_" + usuarioActualAux.getNombre() + "_" + fechaInicioFormateada + ".xlsx";
            }
        } else {
            if(usuarioActualAux.getId() == -1) {
        		nombreArchivo = "FICHAJES_GENERAL_" + fechaInicioFormateada + "_" + fechaFinFormateada + ".xlsx";
            } else {
                nombreArchivo = "FICHAJES_" + usuarioActualAux.getNombre() + "_" + fechaInicioFormateada + "_" + fechaFinFormateada + ".xlsx";
            }
        }
        
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            workbook.write(byteArrayOutputStream);
            byte[] excelBytes = byteArrayOutputStream.toByteArray();

            String base64Excel = java.util.Base64.getEncoder().encodeToString(excelBytes);
            UI.getCurrent().getPage().executeJs(
                "var link = document.createElement('a');" +
                "link.href = 'data:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;base64,' + $0;" +
                "link.download = '" + nombreArchivo + "';" +
                "link.click();", base64Excel);
        } catch (IOException e) {
            Notification.show("Error al generar el archivo Excel");
        }
    }
    
    private void generarLogExcel() {
    	LocalDate fechaInicioValor = fechaInicio.getValue() != null ? fechaInicio.getValue() : LocalDate.now();
        LocalDate fechaFinValor = fechaFin.getValue() != null ? fechaFin.getValue() : LocalDate.now();

        List<Logs_modificaciones> logs = logs_modificacionesRepositorio.findByFechaBetween(fechaInicioValor, fechaFinValor);
        List<Logs_modificaciones> logsFiltrados = logs.stream().filter(log -> log.getRegistro() != null).filter(log -> {
                if (usuarioActualAux.getId() == -1) {
                    return log.getRegistro().getUsuarioId().getEmpresa().getId().equals(usuarioActual.getEmpresa().getId());
                } else {
                    return log.getRegistro().getUsuarioId().getId().equals(usuarioActualAux.getId());
                }
            }).collect(Collectors.toList());

        if (logsFiltrados.isEmpty()) {
            Notification.show("No hay registros para generar el EXCEL", 2000, Notification.Position.TOP_CENTER);
            return;
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Registros");

        XSSFRow titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("FichaWeb");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        XSSFRow userRow = sheet.createRow(1);
        if(usuarioActualAux.getId() == -1) {
        	userRow.createCell(0).setCellValue("Usuario: " + usuarioActual.getNombre() + " (" + usuarioActual.getEmpresa().getNombreComercial() + ")");
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));
        } else {
        	userRow.createCell(0).setCellValue("Usuario: " + usuarioActualAux.getNombre() + " (" + usuarioActualAux.getEmpresa().getNombreComercial() + ")");
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaInicioFormateada = fechaInicioValor.format(formatter);
        String fechaFinFormateada = fechaFinValor.format(formatter);

        String rangoFechas;
        if (fechaInicioValor.equals(fechaFinValor)) {
            rangoFechas = "Fecha: " + fechaInicioFormateada;
        } else {
            rangoFechas = "Fecha: " + fechaInicioFormateada + " - " + fechaFinFormateada;
        }

        XSSFRow dateRow = sheet.createRow(2);
        dateRow.createCell(0).setCellValue(rangoFechas);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 4));

        XSSFRow headerRow = sheet.createRow(3);
        headerRow.createCell(0).setCellValue("FECHA");
        headerRow.createCell(1).setCellValue("MODIFICADO");
        headerRow.createCell(2).setCellValue("VALOR PREVIO");
        headerRow.createCell(3).setCellValue("VALOR NUEVO");
        if (usuarioActualAux.getId() == -1) {
            headerRow.createCell(4).setCellValue("USUARIO");
        }

        int rowIndex = 4;
        for (Logs_modificaciones log : logsFiltrados) {
            XSSFRow row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(log.getFecha().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            row.createCell(1).setCellValue(log.getCampo());
            row.createCell(2).setCellValue(log.getValorPrevio());
            row.createCell(3).setCellValue(log.getValorNuevo());
            if (usuarioActualAux.getId() == -1) {
                row.createCell(4).setCellValue(log.getRegistro().getUsuarioId().getLoginUsuario());
            }
        }

        int totalColumns = (usuarioActualAux.getId() == -1) ? 5 : 4;
        for (int i = 0; i < totalColumns; i++) {
            sheet.autoSizeColumn(i);
        }

        String nombreArchivo;
        if (fechaInicioValor.equals(fechaFinValor)) {
        	if(usuarioActualAux.getId() == -1) {
        		nombreArchivo = "LOGS_GENERAL_" + fechaInicioFormateada + ".xlsx";
            } else {
            	nombreArchivo = "LOGS_" + usuarioActualAux.getNombre() + "_" + fechaInicioFormateada + ".xlsx";
            }
        } else {
        	if(usuarioActualAux.getId() == -1) {
        		nombreArchivo = "LOGS_GENERAL_" + fechaInicioFormateada + "_" + fechaFinFormateada + ".xlsx";
            } else {
            	nombreArchivo = "LOGS_" + usuarioActualAux.getNombre() + "_" + fechaInicioFormateada + "_" + fechaFinFormateada + ".xlsx";
            }
        }
        
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            workbook.write(byteArrayOutputStream);
            byte[] excelBytes = byteArrayOutputStream.toByteArray();

            String base64Excel = java.util.Base64.getEncoder().encodeToString(excelBytes);
            UI.getCurrent().getPage().executeJs(
                "var link = document.createElement('a');" +
                "link.href = 'data:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;base64,' + $0;" +
                "link.download = '" + nombreArchivo + "';" +
                "link.click();", base64Excel);
        } catch (IOException e) {
            Notification.show("Error al generar el archivo Excel");
        }
    }
}