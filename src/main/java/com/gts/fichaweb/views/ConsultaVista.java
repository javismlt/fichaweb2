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

@Route("consulta")
@CssImport(value = "./themes/my-theme/styles.css", themeFor = "vaadin-grid") 
public class ConsultaVista extends AppLayout {
    private final UsuarioRepositorio usuarioRepositorio;
    private final RegistroRepositorio registroRepositorio;
    private Usuario usuarioActual;
    private Grid<Registro> grid;
    private Span totalHorasTrabajadasLabel;
    private DatePicker fechaInicio;
    private DatePicker fechaFin;

    public ConsultaVista(UsuarioRepositorio usuarioRepositorio, RegistroRepositorio registroRepositorio) {
        this.registroRepositorio = registroRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;

        String nombreUsuario = (String) VaadinSession.getCurrent().getAttribute("username");
        if (nombreUsuario == null) {
            getElement().executeJs("window.location.href='/'");
        } else {
            this.usuarioActual = usuarioRepositorio.findByLoginUsuario(nombreUsuario);
            crearHeader(nombreUsuario);
            crearContenido();
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

    private void crearContenido() {
        fechaInicio = new DatePicker("Fecha inicio");
        fechaFin = new DatePicker("Fecha fin");
        fechaFin.setValue(LocalDate.now());

        fechaInicio.setLocale(new java.util.Locale("es", "ES"));
        fechaFin.setLocale(new java.util.Locale("es", "ES"));

        HorizontalLayout fechasLayout = new HorizontalLayout(fechaInicio, fechaFin);
        fechasLayout.setAlignItems(Alignment.CENTER);
        fechasLayout.addClassName("fechas-layout");

        HorizontalLayout selectUsuarioLayout = new HorizontalLayout();
        selectUsuarioLayout.setAlignItems(Alignment.CENTER);
        selectUsuarioLayout.setSpacing(true);

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
                        usuarioActual = usuarioSeleccionado;
                        actualizarGrid(fechaInicio.getValue(), fechaFin.getValue());
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
            selectUsuarioLayout.add(selectUsuarios);
        }

        grid = new Grid<>(Registro.class); 
        grid.setHeightFull();
        grid.removeAllColumns();
        grid.addClassName("responsive-grid");
        
        Grid.Column<Registro> fechaColumn = grid.addColumn(Registro::getFechaRegistro).setHeader("Fecha").setAutoWidth(true);
        Grid.Column<Registro> accionColumn = grid.addColumn(new ComponentRenderer<>(registro -> {
            Span span = new Span(registro.getAccion());
            if ("entrada".equalsIgnoreCase(registro.getAccion())) {
                span.getStyle().set("color", "green");
            } else if ("salida".equalsIgnoreCase(registro.getAccion())) {
                span.getStyle().set("color", "red");
            }
            return span;
        })).setHeader("Acción").setAutoWidth(true);
        accionColumn.getElement().getClassList().add("accion-column");
        grid.addColumn(registro -> {LocalTime hora = registro.getHora();return hora != null ? hora.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "";}).setHeader("Hora");
        grid.addColumn(Registro::getObservaciones).setHeader("Observaciones");
        
        fechaInicio.addValueChangeListener(event -> actualizarGrid(fechaInicio.getValue(), fechaFin.getValue()));
        fechaFin.addValueChangeListener(event -> actualizarGrid(fechaInicio.getValue(), fechaFin.getValue()));
        
        totalHorasTrabajadasLabel = new Span("");
        totalHorasTrabajadasLabel.getStyle().set("font-weight", "bold").set("font-size", "16px");
        totalHorasTrabajadasLabel.addClassName("horas-trabajadas-label");
        
        Button descargarPdf = new Button("Descargar PDF", e -> generarPdf(grid));
        Button descargarExcel = new Button("Descargar EXCEL", e -> generarExcel(grid));

        HorizontalLayout botonesLayout = new HorizontalLayout(descargarPdf, descargarExcel);
        botonesLayout.setAlignItems(Alignment.CENTER);
        botonesLayout.addClassName("botones-layout");
        
        descargarPdf.getStyle().set("margin-top", "37px").set("color", "white").set("background-color", "red").set("cursor", "pointer");
        descargarExcel.getStyle().set("margin-top", "37px").set("color", "white").set("background-color", "green").set("cursor", "pointer");

        fechasLayout.add(botonesLayout);

        VerticalLayout contenidoPrincipal = new VerticalLayout(selectUsuarioLayout, fechasLayout, totalHorasTrabajadasLabel, grid); 
        contenidoPrincipal.setPadding(true);
        contenidoPrincipal.setAlignItems(Alignment.START);
        contenidoPrincipal.setWidthFull();
        contenidoPrincipal.setHeightFull();
        
        setContent(contenidoPrincipal);
        actualizarGrid(fechaInicio.getValue(), fechaFin.getValue());
    }


    private void actualizarGrid(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Registro> registros;
        if (fechaInicio != null && fechaFin != null) {
            registros = registroRepositorio.findByFechaRegistroBetweenAndUsuario_Id(fechaInicio, fechaFin, usuarioActual.getId().intValue());
        } else {
            registros = registroRepositorio.findByUsuario_Id(usuarioActual.getId().intValue());
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

        Paragraph usuarioParrafo = new Paragraph("Usuario: " + usuarioActual.getNombre() + " (" + usuarioActual.getEmpresa().getNombreComercial() + ")").setFontSize(12);
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
            table.addCell(new Cell().add(new Paragraph(registro.getFechaRegistro().toString())));
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
            nombreArchivo = "FICHAJES_" + usuarioActual.getNombre() + "_" + fechaInicioFormateada + ".pdf";
        } else {
            nombreArchivo = "FICHAJES_" + usuarioActual.getNombre() + "_" + fechaInicioFormateada + "_" + fechaFinFormateada + ".pdf";
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
        userRow.createCell(0).setCellValue("Usuario: " + usuarioActual.getNombre() + " (" + usuarioActual.getEmpresa().getNombreComercial() + ")");
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
            nombreArchivo = "FICHAJES_" + usuarioActual.getNombre() + "_" + fechaInicioFormateada + ".xlsx";
        } else {
            nombreArchivo = "FICHAJES_" + usuarioActual.getNombre() + "_" + fechaInicioFormateada + "_" + fechaFinFormateada + ".xlsx";
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
}

