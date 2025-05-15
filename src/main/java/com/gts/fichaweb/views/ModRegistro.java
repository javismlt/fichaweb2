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
import java.util.Comparator;
import com.vaadin.flow.component.dependency.CssImport;
import modelos.Logs_modificaciones;
import repositorios.Logs_modificacionesRepositorio;
import java.util.ArrayList;
import java.util.Collections;

@Route("modregistros")
@CssImport(value = "./themes/my-theme/styles.css", themeFor = "vaadin-grid") 
public class ModRegistro extends AppLayout {
    private final UsuarioRepositorio usuarioRepositorio;
    private final RegistroRepositorio registroRepositorio;
    private final Logs_modificacionesRepositorio logs_modificacionesRepositorio;  
    private Usuario usuarioActual;
    private Usuario usuarioActualAux;
    private Grid<Registro> grid;
    private Span totalHorasTrabajadasLabel;
    private DatePicker fechaInicio;
    private DatePicker fechaFin;
    private Button botonUsuario;

    public ModRegistro(UsuarioRepositorio usuarioRepositorio, RegistroRepositorio registroRepositorio, Logs_modificacionesRepositorio logs_modificacionesRepositorio) {
        this.registroRepositorio = registroRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.logs_modificacionesRepositorio = logs_modificacionesRepositorio; 
        
        String nombreUsuario = (String) VaadinSession.getCurrent().getAttribute("username");
        if (nombreUsuario == null) {
            getElement().executeJs("window.location.href='/'");
        } else {
            this.usuarioActual = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
            if (usuarioActual.getRol() == 1 || usuarioActual.getRol() == 2) {
            	crearHeader(nombreUsuario);
                crearContenido();
            } else {
            	Notification.show("Usuario no administrador", 2000, Notification.Position.TOP_CENTER);
                getElement().executeJs("setTimeout(() => window.location.href='/', 2000)");
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
        
        Anchor enlaceRegistros = new Anchor("registro", "Registros");
        enlaceRegistros.getElement().setAttribute("href", "/modregistros");
        enlaceRegistros.getStyle().set("color", "black").set("text-decoration", "none").set("font-size", "16px");

        HorizontalLayout menuIzquierdo = new HorizontalLayout();
        if (usuarioActual.getRol() != 2) {
            menuIzquierdo.add(botonEmpresa);
        }
        
        menuIzquierdo.add(botonUsuario, enlaceEmpresas, enlaceUsuarios, enlaceRegistros);
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
        
        Select<Integer> selectValidado = new Select<>();
        selectValidado.setLabel("Estado");
        selectValidado.setItems(1, 0);
        selectValidado.setItemLabelGenerator(value -> value == 1 ? "Validado" : "No Validado");
        
        selectValidado.addValueChangeListener(event -> { 
            actualizarGrid(fechaInicio.getValue(), fechaFin.getValue(), selectValidado.getValue());
        });
    
        
        HorizontalLayout fechasLayout = new HorizontalLayout(selectUsuarios, fechaInicio, fechaFin, selectValidado);
        fechasLayout.setAlignItems(Alignment.CENTER);
        fechasLayout.addClassName("fechas-layout");
        fechasLayout.setWidthFull();
        
	    List<Usuario> usuariosDeLaEmpresa = new ArrayList<>();
	
	    if (usuarioActual.getRol() == 1) {
	    	usuariosDeLaEmpresa = usuarioRepositorio.findAll().stream().filter(usuario -> usuario.getRol() != 3 && usuario.getActivo() != 0).collect(Collectors.toList());
	    } else {
	        usuariosDeLaEmpresa = usuarioRepositorio.findByEmpresaId(usuarioActual.getEmpresa().getId()).stream().filter(usuario -> usuario.getRol() != 3 && usuario.getActivo() != 0).collect(Collectors.toList());
	    }
	
	    selectUsuarios.setItems(usuariosDeLaEmpresa);
	    selectUsuarios.setItemLabelGenerator(Usuario::getNombre);
	    selectUsuarios.setPlaceholder("Seleccione un usuario");
	
	    selectUsuarios.addValueChangeListener(event -> {
	        Usuario usuarioSeleccionado = event.getValue();
	        if (usuarioSeleccionado != null) {
	            System.out.println("Usuario seleccionado: " + usuarioSeleccionado.getNombre());
	            usuarioActualAux = usuarioSeleccionado;
                actualizarGrid(fechaInicio.getValue(), fechaFin.getValue(), selectValidado.getValue());
                selectValidado.clear();
	        }
	    });
	
	    grid = new Grid<>(Registro.class); 
        grid.setHeightFull();
        grid.removeAllColumns();
        grid.addClassName("responsive-grid");
        
        grid.addColumn(registro -> registro.getFechaRegistro().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).setHeader("Fecha").setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(registro -> {
            Span span = new Span(registro.getAccion());
            if (Integer.valueOf(1).equals(registro.getValidado())) {
                span.getStyle().set("color", "black");
            } else if (Integer.valueOf(0).equals(registro.getValidado())) {
                span.getStyle().set("color", "red");
            }
            return span;
        })).setHeader("Acción").setAutoWidth(true);
        grid.addColumn(registro -> {LocalTime hora = registro.getHora();return hora != null ? hora.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "";}).setHeader("Hora");
        grid.addColumn(Registro::getObservaciones).setHeader("Observaciones");
        
        grid.addSelectionListener(event -> {
            event.getFirstSelectedItem().ifPresent(registro -> {
                Dialog dialog = new Dialog();
                dialog.setWidth("500px");

                dialog.setCloseOnEsc(true);
                dialog.setCloseOnOutsideClick(true);

                Span titulo = new Span("Detalles del Registro");
                titulo.getStyle().set("font-weight", "bold").set("font-size", "18px");

                VerticalLayout contenido = new VerticalLayout();
                
                Span fechaSpan = new Span("Fecha: " + registro.getFechaRegistro());
                Span accionSpan = new Span("Acción: " + registro.getAccion());
                Span observacionesSpan = new Span("Observaciones: " + registro.getObservaciones());

                LocalTime hora = registro.getHora();
                String horaFormateada = hora != null ? hora.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "N/A";
                
                Span horaSpan = new Span("Hora: " + horaFormateada);
                TextField horaTextField = new TextField("Hora");
                horaTextField.setValue(horaFormateada);
                horaTextField.setVisible(false);  
                horaTextField.setWidthFull();
                
                contenido.add(fechaSpan, accionSpan, horaSpan, horaTextField, observacionesSpan);

                Button actualizar = new Button("Actualizar");
                actualizar.setVisible(false);  
                actualizar.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer");

                Button eliminar = new Button("Eliminar", e -> dialog.close());
                eliminar.getStyle().set("background-color", "red").set("color", "white").set("cursor", "pointer");
                eliminar.addClickListener(e -> {
                	registro.setActivo(0); 
                	registroRepositorio.save(registro);
                    Notification.show("Registro eliminado correctamente", 2000, Notification.Position.TOP_CENTER);
                    actualizarGrid(fechaInicio.getValue(), fechaFin.getValue(), selectValidado.getValue());
                    dialog.close();
                });
                
                Button modificar = new Button("Modificar");
                modificar.getStyle().set("background-color", "#007BFF").set("color", "white").set("cursor", "pointer");

                Button validar = new Button();
	            if (registro.getValidado() == 1) {
	                validar.setText("Validado");
	                validar.getStyle().set("background-color", "green").set("color", "white").set("cursor", "pointer");
	            } else {
	                validar.setText("Validar");
	                validar.getStyle().set("background-color", "green").set("color", "white").set("cursor", "pointer");
	
	                validar.addClickListener(e -> {
	                    registro.setValidado(1); 
	                    registroRepositorio.save(registro);
	                    Notification.show("Registro validado correctamente", 2000, Notification.Position.TOP_CENTER);
	                    dialog.close();
	                });
	            }
             
                modificar.addClickListener(e -> {
                    horaSpan.setVisible(false);
                    horaTextField.setVisible(true);
                    modificar.setVisible(false);
                    validar.setVisible(false);
                    actualizar.setVisible(true);
                    eliminar.setVisible(false);
                });

                actualizar.addClickListener(e -> {
                    LocalTime horaPrevia = registro.getHora();
                    LocalTime nuevaHora = LocalTime.parse(horaTextField.getValue(), DateTimeFormatter.ofPattern("HH:mm:ss"));
                    String horaPreviaStr = horaPrevia != null ? horaPrevia.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "N/A";
                    String nuevaHoraStr = nuevaHora != null ? nuevaHora.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "N/A";
                    registro.setHora(nuevaHora);
                    registroRepositorio.save(registro);
                    String nombreCampo = "hora"; 
                    registroLog(registro, nombreCampo, horaPreviaStr, nuevaHoraStr, selectValidado.getValue());

                    Notification.show("Registro actualizado correctamente", 2000, Notification.Position.TOP_CENTER);
                    dialog.close(); 
                });
                HorizontalLayout botones = new HorizontalLayout(eliminar, modificar, actualizar, validar);
                dialog.add(titulo, contenido, botones);
                dialog.open();
            });
        });

        fechaInicio.addValueChangeListener(event -> actualizarGrid(fechaInicio.getValue(), fechaFin.getValue(), selectValidado.getValue()));
        fechaFin.addValueChangeListener(event -> actualizarGrid(fechaInicio.getValue(), fechaFin.getValue(), selectValidado.getValue()));
        
        totalHorasTrabajadasLabel = new Span("");
        totalHorasTrabajadasLabel.getStyle().set("font-weight", "bold").set("font-size", "16px");
        totalHorasTrabajadasLabel.addClassName("horas-trabajadas-label");
        
        Button descargarPdf = new Button("Descargar PDF", e -> generarPdf(grid));
        Button descargarExcel = new Button("Descargar EXCEL", e -> generarExcel(grid));

        HorizontalLayout botonesLayout = new HorizontalLayout(descargarPdf, descargarExcel);
        botonesLayout.setAlignItems(Alignment.CENTER);
        botonesLayout.addClassName("botones-layout");
        botonesLayout.setWidthFull();
        
        descargarPdf.getStyle().set("margin-top", "37px").set("color", "white").set("background-color", "red").set("cursor", "pointer");
        descargarExcel.getStyle().set("margin-top", "37px").set("color", "white").set("background-color", "green").set("cursor", "pointer");

        fechasLayout.add(botonesLayout);

        VerticalLayout contenidoPrincipal = new VerticalLayout(fechasLayout, totalHorasTrabajadasLabel, grid); 
        contenidoPrincipal.setPadding(true);
        contenidoPrincipal.setAlignItems(Alignment.START);
        contenidoPrincipal.setWidthFull();
        contenidoPrincipal.setHeightFull();
        
        setContent(contenidoPrincipal);
        actualizarGrid(fechaInicio.getValue(), fechaFin.getValue(), selectValidado.getValue());
    }


    private void actualizarGrid(LocalDate fechaInicio, LocalDate fechaFin, Integer selectValidado) {
        List<Registro> registros;
        if (usuarioActualAux == null) {
            registros = Collections.emptyList();
        } else if (fechaInicio != null && fechaFin != null) {
        	if (selectValidado == null) {
        		registros = registroRepositorio.findByFechaRegistroBetweenAndUsuario_IdAndActivo(fechaInicio, fechaFin, usuarioActualAux.getId().intValue(),1);
        	} else {
                registros = registroRepositorio.findByFechaRegistroBetweenAndUsuario_IdAndActivoAndValidado(fechaInicio, fechaFin, usuarioActualAux.getId().intValue(), 1, selectValidado);
            }
        } else {
        	if (selectValidado == null) {
        		registros = registroRepositorio.findByUsuario_IdAndActivo(usuarioActualAux.getId().intValue(),1);
        	} else {
                registros = registroRepositorio.findByUsuario_IdAndActivoAndValidado(usuarioActualAux.getId().intValue(), 1, selectValidado);
            }
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
        Duration totalDuration = Duration.ZERO;
        Duration totalDescanso = Duration.ZERO;

        for (Registro salida : registros) {
            if ("salida".equalsIgnoreCase(salida.getAccion()) && salida.getIdAsociado() != null) {
                Registro entrada = registros.stream()
                        .filter(r -> r.getId().equals(salida.getIdAsociado()))
                        .findFirst()
                        .orElse(null);

                if (entrada != null && entrada.getHora() != null && salida.getHora() != null) {
                    Duration duration = Duration.between(entrada.getHora(), salida.getHora());
                    totalDuration = totalDuration.plus(duration);
                }
            }

            if ("pausa".equalsIgnoreCase(salida.getAccion()) && salida.getHora() != null) {
                Registro reanudacion = registros.stream()
                        .filter(r -> "reanudacion".equalsIgnoreCase(r.getAccion()) && salida.getId().equals(r.getIdAsociado()))
                        .findFirst()
                        .orElse(null);

                if (reanudacion != null && reanudacion.getHora() != null) {
                    Duration descanso = Duration.between(salida.getHora(), reanudacion.getHora());
                    totalDescanso = totalDescanso.plus(descanso);
                }
            }
        }

        totalDuration = totalDuration.minus(totalDescanso);

        long horas = totalDuration.toHours();
        long minutos = (totalDuration.toMinutes() % 60);

        return String.format("%02d:%02d", horas, minutos);
    }

    private void generarPdf(Grid<Registro> grid) {
        List<Registro> registros = grid.getListDataView().getItems().collect(Collectors.toList());

        if (registros.isEmpty()) {
            Notification.show("No hay registros para generar el PDF");
            return;
        }
        
        String horasTrabajadas = calcularHorasTrabajadas(registros);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        Paragraph titulo = new Paragraph("FichaWeb").setFontSize(28) .setBold(); 
        document.add(titulo);

        Paragraph usuarioParrafo = new Paragraph("Usuario: " + usuarioActualAux.getNombre() + " (" + usuarioActualAux.getEmpresa().getNombreComercial() + ")").setFontSize(12);
        document.add(usuarioParrafo);

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
        
        Table table = new Table(5);
        table.addHeaderCell(new Cell().add(new Paragraph("FECHA")));
        table.addHeaderCell(new Cell().add(new Paragraph("ACCION")));
        table.addHeaderCell(new Cell().add(new Paragraph("HORA")));
        table.addHeaderCell(new Cell().add(new Paragraph("ORIGEN")));
        table.addHeaderCell(new Cell().add(new Paragraph("OBSERVACIONES")));

        for (Registro registro : registros) {
        	table.addCell(new Cell().add(new Paragraph(registro.getFechaRegistro().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))));
            table.addCell(new Cell().add(new Paragraph(registro.getAccion())));
            table.addCell(new Cell().add(new Paragraph(registro.getHora() != null ? registro.getHora().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "")));
            table.addCell(new Cell().add(new Paragraph(registro.getOrigen())));
            table.addCell(new Cell().add(new Paragraph(registro.getObservaciones())));
        }

        document.add(table);
        document.close();

        byte[] pdfBytes = byteArrayOutputStream.toByteArray();
        String base64Pdf = java.util.Base64.getEncoder().encodeToString(pdfBytes);

        String nombreArchivo;
        if (fechaInicioValor.equals(fechaFinValor)) {
            nombreArchivo = "FICHAJES_" + usuarioActualAux.getNombre() + "_" + fechaInicioFormateada + ".pdf";
        } else {
            nombreArchivo = "FICHAJES_" + usuarioActualAux.getNombre() + "_" + fechaInicioFormateada + "_" + fechaFinFormateada + ".pdf";
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
            Notification.show("No hay registros para generar el archivo Excel");
            return;
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Registros");

        XSSFRow titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("FichaWeb");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        XSSFRow userRow = sheet.createRow(1);
        userRow.createCell(0).setCellValue("Usuario: " + usuarioActualAux.getNombre() + " (" + usuarioActualAux.getEmpresa().getNombreComercial() + ")");
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

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
        headerRow.createCell(4).setCellValue("OBSERVACIONES");

        int rowIndex = 5; 
        for (Registro registro : registros) {
            XSSFRow row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(registro.getFechaRegistro().format(formatter));
            row.createCell(1).setCellValue(registro.getAccion());
            row.createCell(2).setCellValue(registro.getHora() != null ? registro.getHora().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "");
            row.createCell(4).setCellValue(registro.getOrigen());
            row.createCell(4).setCellValue(registro.getObservaciones());
        }

        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        String nombreArchivo;
        if (fechaInicioValor.equals(fechaFinValor)) {
            nombreArchivo = "FICHAJES_" + usuarioActualAux.getNombre() + "_" + fechaInicioFormateada + ".xlsx";
        } else {
            nombreArchivo = "FICHAJES_" + usuarioActualAux.getNombre() + "_" + fechaInicioFormateada + "_" + fechaFinFormateada + ".xlsx";
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
    
    private void agregarNumero(TextField pinField, String numero) {
        String total = pinField.getValue();
        if (total.length() < 4) {
            pinField.setValue(total + numero);
        }
    }
    
    private void registroLog(Registro registro, String campo, String valorPrevio, String valorNuevo, Integer selectValidado) {
    	Logs_modificaciones registroLog = new Logs_modificaciones();
    	registroLog.setFecha(LocalDate.now());
    	registroLog.setRegistro(registro);
    	registroLog.setCampo(campo);
    	registroLog.setValorPrevio(valorPrevio);
    	registroLog.setValorNuevo(valorNuevo);
    	
    	logs_modificacionesRepositorio.save(registroLog);
    	actualizarGrid(fechaInicio.getValue(), fechaFin.getValue(), selectValidado);
    }
}

