package com.bikeshop.service;

import com.bikeshop.entity.Factura;
import com.bikeshop.entity.Mantenimiento;
import com.bikeshop.entity.Pedido;
import com.bikeshop.entity.Producto;
import com.bikeshop.repository.FacturaRepository;
import com.bikeshop.repository.MantenimientoRepository;
import com.bikeshop.repository.PedidoRepository;
import com.bikeshop.repository.ProductoRepository;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteExportService {

    private final PedidoRepository pedidoRepo;
    private final MantenimientoRepository mantenimientoRepo;
    private final ProductoRepository productoRepo;
    private final FacturaRepository facturaRepo;

    // ─── Colores corporativos ────────────────────────────────────────────────
    private static final Color ROJO       = new Color(0xE3, 0x18, 0x37);
    private static final Color OSCURO     = new Color(0x14, 0x14, 0x14);
    private static final Color GRIS       = new Color(0xA0, 0xA0, 0xA0);
    private static final Color GRIS_CLARO = new Color(0xF5, 0xF5, 0xF5);
    private static final Color VERDE      = new Color(0x05, 0x96, 0x69);
    private static final Color AZUL       = new Color(0x1A, 0x56, 0xDB);
    private static final Color NARANJA    = new Color(0xD9, 0x75, 0x06);

    // ══════════════════════════════════════════════════════════════════════════
    //  EXCEL
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public byte[] generarExcel(String tipo, LocalDate fecha, int anio, int mes) {
        DatosReporte datos = cargarDatos(tipo, fecha, anio, mes);

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            crearHojaResumen(wb, datos);
            crearHojaPedidos(wb, datos.pedidos);
            crearHojaFacturas(wb, datos.facturas);
            crearHojaMantenimientos(wb, datos.mantenimientos);
            crearHojaInventario(wb);

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando Excel: " + e.getMessage(), e);
        }
    }

    // ── Hoja 1: Resumen ──────────────────────────────────────────────────────
    private void crearHojaResumen(XSSFWorkbook wb, DatosReporte datos) {
        XSSFSheet sheet = wb.createSheet("Resumen");
        sheet.setColumnWidth(0, 9000);
        sheet.setColumnWidth(1, 7000);

        CellStyle titleStyle  = estiloTitulo(wb);
        CellStyle headerStyle = estiloEncabezado(wb);
        CellStyle valorStyle  = estiloValor(wb);
        CellStyle monedaStyle = estiloMoneda(wb);
        CellStyle labelStyle  = estiloLabel(wb);
        CellStyle filaNormal  = estiloFilaNormal(wb);
        CellStyle filaAlterna = estiloFilaAlterna(wb);

        int r = 0;

        // Título
        Row rowTitulo = sheet.createRow(r++);
        rowTitulo.setHeightInPoints(32);
        Cell cTitulo = rowTitulo.createCell(0);
        cTitulo.setCellValue("BIKESHOP — Reporte de Gestión");
        cTitulo.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        Row rowPeriodo = sheet.createRow(r++);
        rowPeriodo.setHeightInPoints(20);
        Cell cPeriodo = rowPeriodo.createCell(0);
        cPeriodo.setCellValue("Período: " + datos.periodo);
        cPeriodo.setCellStyle(labelStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));

        Row rowFecha = sheet.createRow(r++);
        Cell cFecha = rowFecha.createCell(0);
        cFecha.setCellValue("Generado: " + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        cFecha.setCellStyle(labelStyle);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 1));

        r++; // espacio

        // Encabezados
        Row rowHead = sheet.createRow(r++);
        rowHead.setHeightInPoints(20);
        Cell h1 = rowHead.createCell(0); h1.setCellValue("Métrica"); h1.setCellStyle(headerStyle);
        Cell h2 = rowHead.createCell(1); h2.setCellValue("Valor");   h2.setCellStyle(headerStyle);

        // Métricas — incluye ventas presenciales
        Object[][] metricas = {
            {"Ingresos totales",                     datos.totalGeneral.doubleValue()},
            {"Ingresos por ventas web (pedidos)",    datos.totalWeb.doubleValue()},
            {"Ingresos por ventas presencial",       datos.totalPresencial.doubleValue()},
            {"Ingresos por mantenimiento",           datos.totalMant.doubleValue()},
            {"Cantidad de pedidos web",              (double) datos.pedidos.size()},
            {"Cantidad de facturas presencial",      (double) datos.facturas.size()},
            {"Cantidad de mantenimientos",           (double) datos.mantenimientos.size()},
            {"Ticket promedio (pedidos web)",        datos.ticketPromedio.doubleValue()},
        };

        boolean alterno = false;
        for (Object[] fila : metricas) {
            Row row = sheet.createRow(r++);
            row.setHeightInPoints(18);

            Cell cLabel = row.createCell(0);
            cLabel.setCellValue((String) fila[0]);
            cLabel.setCellStyle(alterno ? filaAlterna : filaNormal);

            Cell cVal = row.createCell(1);
            cVal.setCellValue((double) fila[1]);
            boolean esMoneda = !((String) fila[0]).startsWith("Cantidad");
            cVal.setCellStyle(esMoneda ? monedaStyle : valorStyle);
            alterno = !alterno;
        }
    }

    // ── Hoja 2: Pedidos web ──────────────────────────────────────────────────
    private void crearHojaPedidos(XSSFWorkbook wb, List<Pedido> pedidos) {
        XSSFSheet sheet = wb.createSheet("Pedidos");
        int[] anchos = {3000, 6000, 8000, 7000, 5000, 5000, 5000};
        for (int i = 0; i < anchos.length; i++) sheet.setColumnWidth(i, anchos[i]);

        CellStyle headerStyle = estiloEncabezado(wb);
        CellStyle monedaStyle = estiloMoneda(wb);
        CellStyle filaNormal  = estiloFilaNormal(wb);
        CellStyle filaAlterna = estiloFilaAlterna(wb);

        Row head = sheet.createRow(0);
        head.setHeightInPoints(20);
        String[] cols = {"#", "Fecha", "Cliente", "Email", "Items", "Estado", "Total"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = head.createCell(i); c.setCellValue(cols[i]); c.setCellStyle(headerStyle);
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        boolean alterno = false;
        for (int i = 0; i < pedidos.size(); i++) {
            Pedido p = pedidos.get(i);
            Row row = sheet.createRow(i + 1);
            row.setHeightInPoints(16);
            CellStyle base = alterno ? filaAlterna : filaNormal;

            celd(row, 0, String.valueOf(p.getId()), base);
            celd(row, 1, p.getCreadoEn() != null ? p.getCreadoEn().format(dtf) : "", base);
            celd(row, 2, p.getNombreCliente() != null ? p.getNombreCliente() : "", base);
            celd(row, 3, p.getEmailCliente() != null ? p.getEmailCliente() : "", base);
            celd(row, 4, String.valueOf(p.getItems() != null ? p.getItems().size() : 0), base);
            celd(row, 5, p.getEstado() != null ? p.getEstado().name() : "", base);

            Cell cTotal = row.createCell(6);
            cTotal.setCellValue(p.getTotal() != null ? p.getTotal().doubleValue() : 0);
            cTotal.setCellStyle(monedaStyle);
            alterno = !alterno;
        }

        if (pedidos.isEmpty()) {
            Row vacio = sheet.createRow(1);
            Cell c = vacio.createCell(0);
            c.setCellValue("Sin pedidos en este período");
            c.setCellStyle(estiloLabel(wb));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));
        }
    }

    // ── Hoja 3: Facturas presenciales ────────────────────────────────────────
    private void crearHojaFacturas(XSSFWorkbook wb, List<Factura> facturas) {
        XSSFSheet sheet = wb.createSheet("Facturas Presencial");
        int[] anchos = {3000, 6000, 8000, 7000, 5000, 5000, 5000};
        for (int i = 0; i < anchos.length; i++) sheet.setColumnWidth(i, anchos[i]);

        CellStyle headerStyle = estiloEncabezado(wb);
        CellStyle monedaStyle = estiloMoneda(wb);
        CellStyle filaNormal  = estiloFilaNormal(wb);
        CellStyle filaAlterna = estiloFilaAlterna(wb);

        Row head = sheet.createRow(0);
        head.setHeightInPoints(20);
        String[] cols = {"#", "Fecha", "Cliente", "Email", "Tipo", "Canal", "Total"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = head.createCell(i); c.setCellValue(cols[i]); c.setCellStyle(headerStyle);
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        boolean alterno = false;
        for (int i = 0; i < facturas.size(); i++) {
            Factura f = facturas.get(i);
            Row row = sheet.createRow(i + 1);
            row.setHeightInPoints(16);
            CellStyle base = alterno ? filaAlterna : filaNormal;

            celd(row, 0, String.valueOf(f.getId()), base);
            celd(row, 1, f.getFechaEmision() != null ? f.getFechaEmision().format(dtf) : "", base);
            celd(row, 2, f.getNombreCliente() != null ? f.getNombreCliente() : "", base);
            celd(row, 3, f.getEmailCliente() != null ? f.getEmailCliente() : "", base);
            celd(row, 4, f.getTipo() != null ? f.getTipo().name() : "", base);
            celd(row, 5, f.getCanal() != null ? f.getCanal().name() : "", base);

            Cell cTotal = row.createCell(6);
            cTotal.setCellValue(f.getTotal() != null ? f.getTotal().doubleValue() : 0);
            cTotal.setCellStyle(monedaStyle);
            alterno = !alterno;
        }

        if (facturas.isEmpty()) {
            Row vacio = sheet.createRow(1);
            Cell c = vacio.createCell(0);
            c.setCellValue("Sin facturas presenciales en este período");
            c.setCellStyle(estiloLabel(wb));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));
        }
    }

    // ── Hoja 4: Mantenimientos ───────────────────────────────────────────────
    private void crearHojaMantenimientos(XSSFWorkbook wb, List<Mantenimiento> mantenimientos) {
        XSSFSheet sheet = wb.createSheet("Mantenimientos");
        int[] anchos = {3000, 5000, 8000, 7000, 6000, 6000, 5000, 5000};
        for (int i = 0; i < anchos.length; i++) sheet.setColumnWidth(i, anchos[i]);

        CellStyle headerStyle = estiloEncabezado(wb);
        CellStyle monedaStyle = estiloMoneda(wb);
        CellStyle filaNormal  = estiloFilaNormal(wb);
        CellStyle filaAlterna = estiloFilaAlterna(wb);

        Row head = sheet.createRow(0);
        head.setHeightInPoints(20);
        String[] cols = {"#", "Fecha", "Cliente", "Email", "Tipo Bici", "Problema", "Estado", "Costo Total"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = head.createCell(i); c.setCellValue(cols[i]); c.setCellStyle(headerStyle);
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        boolean alterno = false;
        for (int i = 0; i < mantenimientos.size(); i++) {
            Mantenimiento m = mantenimientos.get(i);
            Row row = sheet.createRow(i + 1);
            row.setHeightInPoints(16);
            CellStyle base = alterno ? filaAlterna : filaNormal;

            celd(row, 0, String.valueOf(m.getId()), base);
            celd(row, 1, m.getFecha() != null ? m.getFecha().format(dtf) : "", base);
            celd(row, 2, m.getNombreCliente() != null ? m.getNombreCliente() : "", base);
            celd(row, 3, m.getEmailCliente() != null ? m.getEmailCliente() : "", base);
            celd(row, 4, m.getTipoBicicleta() != null ? m.getTipoBicicleta() : "", base);
            String problema = m.getProblemaReportado() != null ? m.getProblemaReportado() : "";
            if (problema.length() > 60) problema = problema.substring(0, 57) + "...";
            celd(row, 5, problema, base);
            celd(row, 6, m.getEstado() != null ? m.getEstado().name() : "", base);

            Cell cTotal = row.createCell(7);
            cTotal.setCellValue(m.getCostoTotal() != null ? m.getCostoTotal().doubleValue() : 0);
            cTotal.setCellStyle(monedaStyle);
            alterno = !alterno;
        }

        if (mantenimientos.isEmpty()) {
            Row vacio = sheet.createRow(1);
            Cell c = vacio.createCell(0);
            c.setCellValue("Sin mantenimientos en este período");
            c.setCellStyle(estiloLabel(wb));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));
        }
    }

    // ── Hoja 5: Inventario ───────────────────────────────────────────────────
    private void crearHojaInventario(XSSFWorkbook wb) {
        XSSFSheet sheet = wb.createSheet("Inventario");
        int[] anchos = {3000, 10000, 7000, 5000, 6000, 5000};
        for (int i = 0; i < anchos.length; i++) sheet.setColumnWidth(i, anchos[i]);

        CellStyle headerStyle = estiloEncabezado(wb);
        CellStyle monedaStyle = estiloMoneda(wb);
        CellStyle alertaStyle = estiloAlerta(wb);
        CellStyle filaNormal  = estiloFilaNormal(wb);
        CellStyle filaAlterna = estiloFilaAlterna(wb);

        Row head = sheet.createRow(0);
        head.setHeightInPoints(20);
        String[] cols = {"#", "Producto", "Categoría", "Stock", "Precio", "Estado"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = head.createCell(i); c.setCellValue(cols[i]); c.setCellStyle(headerStyle);
        }

        List<Producto> productos = productoRepo.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .sorted((a, b) -> Integer.compare(a.getStock(), b.getStock()))
                .collect(Collectors.toList());

        boolean alterno = false;
        for (int i = 0; i < productos.size(); i++) {
            Producto p = productos.get(i);
            Row row = sheet.createRow(i + 1);
            row.setHeightInPoints(16);
            boolean stockBajo = p.getStock() <= 5;
            CellStyle base = stockBajo ? alertaStyle : (alterno ? filaAlterna : filaNormal);

            celd(row, 0, String.valueOf(p.getId()), base);
            celd(row, 1, p.getNombre() != null ? p.getNombre() : "", base);
            String catNombre = "—";
            try { catNombre = p.getCategoria() != null ? p.getCategoria().getNombre() : "—"; }
            catch (Exception ignored) {}
            celd(row, 2, catNombre, base);
            celd(row, 3, String.valueOf(p.getStock()), base);

            Cell cPrecio = row.createCell(4);
            cPrecio.setCellValue(p.getPrecio() != null ? p.getPrecio().doubleValue() : 0);
            cPrecio.setCellStyle(monedaStyle);

            celd(row, 5, stockBajo ? "⚠ Stock bajo" : "OK", base);
            alterno = !alterno;
        }
    }

    // ── Estilos Excel ────────────────────────────────────────────────────────
    private CellStyle estiloTitulo(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setFontHeightInPoints((short) 18); f.setBold(true);
        f.setColor(new XSSFColor(ROJO, null));
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(OSCURO, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.LEFT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private CellStyle estiloEncabezado(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true); f.setFontHeightInPoints((short) 11);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(ROJO, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBottomBorderColor(IndexedColors.WHITE.getIndex());
        return s;
    }

    private CellStyle estiloValor(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont(); f.setBold(true); f.setFontHeightInPoints((short) 11);
        s.setFont(f); s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private CellStyle estiloMoneda(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        DataFormat df = wb.createDataFormat();
        s.setDataFormat(df.getFormat("\"$\"#,##0"));
        XSSFFont f = wb.createFont(); f.setBold(true);
        s.setFont(f); s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    private CellStyle estiloLabel(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setColor(new XSSFColor(GRIS, null)); f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        return s;
    }

    private CellStyle estiloFilaNormal(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setWrapText(false); s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private CellStyle estiloFilaAlterna(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(GRIS_CLARO, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private CellStyle estiloAlerta(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(new Color(0xFF, 0xF3, 0xCD), null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private void celd(Row row, int col, String val, CellStyle style) {
        Cell c = row.createCell(col); c.setCellValue(val); c.setCellStyle(style);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PDF
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public byte[] generarPdf(String tipo, LocalDate fecha, int anio, int mes) {
        DatosReporte datos = cargarDatos(tipo, fecha, anio, mes);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 30, 30, 50, 40);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new PieDePageina(datos.periodo));
            doc.open();

            Font fTitulo   = new Font(Font.HELVETICA, 22, Font.BOLD, ROJO);
            Font fSubtitulo= new Font(Font.HELVETICA, 12, Font.NORMAL, new Color(0xA0, 0xA0, 0xA0));
            Font fNormal   = new Font(Font.HELVETICA,  9, Font.NORMAL, Color.BLACK);
            Font fBold     = new Font(Font.HELVETICA,  9, Font.BOLD,   Color.BLACK);
            Font fBlanco   = new Font(Font.HELVETICA,  9, Font.BOLD,   Color.WHITE);
            Font fAlerta   = new Font(Font.HELVETICA,  9, Font.BOLD,   new Color(0x92, 0x40, 0x00));

            // ── Encabezado ───────────────────────────────────────────────────
            Paragraph titulo = new Paragraph("BIKESHOP", fTitulo);
            titulo.setSpacingAfter(2);
            doc.add(titulo);

            Paragraph sub = new Paragraph(
                    "Reporte de Gestión  ·  Período: " + datos.periodo +
                    "  ·  Generado: " + LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    fSubtitulo);
            sub.setSpacingAfter(14);
            doc.add(sub);

            // ── Tarjetas resumen — 5 columnas ────────────────────────────────
            PdfPTable resumen = new PdfPTable(5);
            resumen.setWidthPercentage(100);
            resumen.setSpacingAfter(12);
            addMetrica(resumen, "INGRESOS TOTALES",      fmt(datos.totalGeneral),    ROJO,   fBlanco);
            addMetrica(resumen, "VENTAS WEB",            fmt(datos.totalWeb),        OSCURO, fBlanco);
            addMetrica(resumen, "VENTAS PRESENCIAL",     fmt(datos.totalPresencial), NARANJA,fBlanco);
            addMetrica(resumen, "MANTENIMIENTOS",        fmt(datos.totalMant),       AZUL,   fBlanco);
            addMetrica(resumen, "TICKET PROM. WEB",      fmt(datos.ticketPromedio),  VERDE,  fBlanco);
            doc.add(resumen);

            // ── Conteos ──────────────────────────────────────────────────────
            PdfPTable conteos = new PdfPTable(4);
            conteos.setWidthPercentage(100);
            conteos.setSpacingAfter(20);
            addConteo(conteos, "Pedidos web",         datos.pedidos.size(),   fBold, fNormal);
            addConteo(conteos, "Facturas presencial", datos.facturas.size(),  fBold, fNormal);
            addConteo(conteos, "Mantenimientos",      datos.mantenimientos.size(), fBold, fNormal);
            addConteo(conteos, "Productos activos",
                    (int) productoRepo.findAll().stream()
                            .filter(p -> Boolean.TRUE.equals(p.getActivo())).count(),
                    fBold, fNormal);
            doc.add(conteos);

            // ── Tabla Pedidos web ────────────────────────────────────────────
            doc.add(seccionTitulo("PEDIDOS WEB DEL PERÍODO", fBold));
            if (datos.pedidos.isEmpty()) {
                doc.add(sinDatos("Sin pedidos en este período", fNormal));
            } else {
                PdfPTable tPedidos = new PdfPTable(new float[]{0.5f, 2f, 3f, 3f, 1f, 2f, 2f});
                tPedidos.setWidthPercentage(100);
                tPedidos.setSpacingAfter(18);
                agregarEncabezadoTabla(tPedidos, fBlanco, "#", "Fecha", "Cliente", "Email", "Items", "Estado", "Total");

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
                boolean alt = false;
                for (Pedido p : datos.pedidos) {
                    Color bg = alt ? GRIS_CLARO : Color.WHITE;
                    agregarFilaTabla(tPedidos, fNormal, bg,
                            String.valueOf(p.getId()),
                            p.getCreadoEn() != null ? p.getCreadoEn().format(dtf) : "",
                            p.getNombreCliente() != null ? p.getNombreCliente() : "",
                            p.getEmailCliente() != null ? p.getEmailCliente() : "",
                            String.valueOf(p.getItems() != null ? p.getItems().size() : 0),
                            p.getEstado() != null ? p.getEstado().name() : "",
                            fmt(p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO));
                    alt = !alt;
                }
                doc.add(tPedidos);
            }

            // ── Tabla Facturas presenciales ──────────────────────────────────
            doc.add(seccionTitulo("VENTAS PRESENCIALES DEL PERÍODO", fBold));
            if (datos.facturas.isEmpty()) {
                doc.add(sinDatos("Sin facturas presenciales en este período", fNormal));
            } else {
                PdfPTable tFact = new PdfPTable(new float[]{0.5f, 2f, 3.5f, 3f, 1.5f, 1.5f, 2f});
                tFact.setWidthPercentage(100);
                tFact.setSpacingAfter(18);
                agregarEncabezadoTabla(tFact, fBlanco, "#", "Fecha", "Cliente", "Email", "Tipo", "Canal", "Total");

                DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
                boolean alt = false;
                for (Factura f : datos.facturas) {
                    Color bg = alt ? GRIS_CLARO : Color.WHITE;
                    agregarFilaTabla(tFact, fNormal, bg,
                            String.valueOf(f.getId()),
                            f.getFechaEmision() != null ? f.getFechaEmision().format(dtf2) : "",
                            f.getNombreCliente() != null ? f.getNombreCliente() : "",
                            f.getEmailCliente() != null ? f.getEmailCliente() : "",
                            f.getTipo() != null ? f.getTipo().name() : "",
                            f.getCanal() != null ? f.getCanal().name() : "",
                            fmt(f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO));
                    alt = !alt;
                }
                doc.add(tFact);
            }

            // ── Tabla Mantenimientos ─────────────────────────────────────────
            doc.add(seccionTitulo("MANTENIMIENTOS DEL PERÍODO", fBold));
            if (datos.mantenimientos.isEmpty()) {
                doc.add(sinDatos("Sin mantenimientos en este período", fNormal));
            } else {
                PdfPTable tMant = new PdfPTable(new float[]{0.5f, 1.5f, 3f, 2.5f, 3f, 2f, 2f});
                tMant.setWidthPercentage(100);
                tMant.setSpacingAfter(18);
                agregarEncabezadoTabla(tMant, fBlanco, "#", "Fecha", "Cliente", "Tipo Bici", "Problema", "Estado", "Costo");

                DateTimeFormatter df2 = DateTimeFormatter.ofPattern("dd/MM/yy");
                boolean alt = false;
                for (Mantenimiento m : datos.mantenimientos) {
                    Color bg = alt ? GRIS_CLARO : Color.WHITE;
                    String problema = m.getProblemaReportado() != null ? m.getProblemaReportado() : "";
                    if (problema.length() > 50) problema = problema.substring(0, 47) + "...";
                    agregarFilaTabla(tMant, fNormal, bg,
                            String.valueOf(m.getId()),
                            m.getFecha() != null ? m.getFecha().format(df2) : "",
                            m.getNombreCliente() != null ? m.getNombreCliente() : "",
                            m.getTipoBicicleta() != null ? m.getTipoBicicleta() : "",
                            problema,
                            m.getEstado() != null ? m.getEstado().name() : "",
                            fmt(m.getCostoTotal() != null ? m.getCostoTotal() : BigDecimal.ZERO));
                    alt = !alt;
                }
                doc.add(tMant);
            }

            // ── Alertas de inventario ────────────────────────────────────────
            List<Producto> stockBajo = productoRepo.findAll().stream()
                    .filter(p -> Boolean.TRUE.equals(p.getActivo()) && p.getStock() <= 5)
                    .sorted((a, b) -> Integer.compare(a.getStock(), b.getStock()))
                    .collect(Collectors.toList());

            doc.add(seccionTitulo("ALERTAS DE INVENTARIO (STOCK ≤ 5)", fBold));
            if (stockBajo.isEmpty()) {
                doc.add(sinDatos("✓ Todos los productos tienen stock suficiente", fNormal));
            } else {
                PdfPTable tInv = new PdfPTable(new float[]{3f, 2f, 1.5f, 2f});
                tInv.setWidthPercentage(100);
                tInv.setSpacingAfter(18);
                agregarEncabezadoTabla(tInv, fBlanco, "Producto", "Categoría", "Stock", "Precio");
                for (Producto p : stockBajo) {
                    String catPdf = "—";
                    try { catPdf = p.getCategoria() != null ? p.getCategoria().getNombre() : "—"; }
                    catch (Exception ignored) {}
                    Color alertaBg = new Color(0xFF, 0xF3, 0xCD);
                    PdfPCell c1 = celda(p.getNombre() != null ? p.getNombre() : "", fAlerta, alertaBg);
                    PdfPCell c2 = celda(catPdf, fAlerta, alertaBg);
                    PdfPCell c3 = celda(String.valueOf(p.getStock()), fAlerta, alertaBg);
                    PdfPCell c4 = celda(fmt(p.getPrecio() != null ? p.getPrecio() : BigDecimal.ZERO), fAlerta, alertaBg);
                    c3.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c4.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    tInv.addCell(c1); tInv.addCell(c2); tInv.addCell(c3); tInv.addCell(c4);
                }
                doc.add(tInv);
            }

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        }
    }

    // ── Helpers PDF ──────────────────────────────────────────────────────────

    private void addMetrica(PdfPTable tabla, String label, String valor, Color fondo, Font fontValor) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(fondo);
        cell.setPadding(10);
        cell.setBorder(Rectangle.NO_BORDER);
        Font fLabel = new Font(Font.HELVETICA, 7, Font.NORMAL, new Color(0xD0, 0xD0, 0xD0));
        Font fValor = new Font(Font.HELVETICA, 13, Font.BOLD, Color.WHITE);
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", fLabel));
        p.add(new Chunk(valor, fValor));
        cell.addElement(p);
        tabla.addCell(cell);
    }

    private void addConteo(PdfPTable tabla, String label, int valor, Font fBold, Font fNormal) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(8);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(new Color(0xE0, 0xE0, 0xE0));
        cell.setBorderWidth(0.5f);
        Paragraph p = new Paragraph();
        p.add(new Chunk(String.valueOf(valor) + "\n", fBold));
        p.add(new Chunk(label, fNormal));
        cell.addElement(p);
        tabla.addCell(cell);
    }

    private Paragraph seccionTitulo(String texto, Font fBold) {
        Font f = new Font(Font.HELVETICA, 10, Font.BOLD, OSCURO);
        Paragraph p = new Paragraph(texto, f);
        p.setSpacingBefore(6); p.setSpacingAfter(4);
        return p;
    }

    private Paragraph sinDatos(String texto, Font fNormal) {
        Paragraph p = new Paragraph(texto, fNormal);
        p.setSpacingAfter(12);
        return p;
    }

    private void agregarEncabezadoTabla(PdfPTable tabla, Font font, String... cols) {
        for (String col : cols) {
            PdfPCell cell = new PdfPCell(new Phrase(col, font));
            cell.setBackgroundColor(OSCURO);
            cell.setPadding(5);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(cell);
        }
    }

    private void agregarFilaTabla(PdfPTable tabla, Font font, Color bg, String... vals) {
        for (String val : vals) tabla.addCell(celda(val, font, bg));
    }

    private PdfPCell celda(String texto, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(4);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(new Color(0xE0, 0xE0, 0xE0));
        cell.setBorderWidth(0.3f);
        return cell;
    }

    // ── Pie de página ────────────────────────────────────────────────────────
    static class PieDePageina extends PdfPageEventHelper {
        private final String periodo;
        PieDePageina(String periodo) { this.periodo = periodo; }

        @Override
        public void onEndPage(PdfWriter writer, Document doc) {
            PdfContentByte cb = writer.getDirectContent();
            Font f = new Font(Font.HELVETICA, 7, Font.NORMAL, new Color(0xA0, 0xA0, 0xA0));
            Phrase pie = new Phrase("BIKESHOP  ·  " + periodo +
                    "  ·  Página " + writer.getPageNumber(), f);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, pie,
                    (doc.right() - doc.left()) / 2 + doc.leftMargin(), doc.bottom() - 10, 0);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CARGA DE DATOS COMUNES
    // ══════════════════════════════════════════════════════════════════════════

    private DatosReporte cargarDatos(String tipo, LocalDate fecha, int anio, int mes) {
        LocalDateTime inicio, fin;
        String periodo;

        switch (tipo) {
            case "semanal" -> {
                LocalDate base = fecha != null ? fecha : LocalDate.now();
                LocalDate lunes   = base.with(DayOfWeek.MONDAY);
                LocalDate domingo = base.with(DayOfWeek.SUNDAY);
                inicio = lunes.atStartOfDay();
                fin    = domingo.atTime(LocalTime.MAX);
                periodo = "Semana " + lunes.format(DateTimeFormatter.ofPattern("dd/MM")) +
                        " – " + domingo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            case "anual" -> {
                int a = anio > 0 ? anio : LocalDate.now().getYear();
                inicio = LocalDate.of(a, 1, 1).atStartOfDay();
                fin    = LocalDate.of(a, 12, 31).atTime(LocalTime.MAX);
                periodo = "Año " + a;
            }
            default -> { // mensual
                LocalDate hoy = LocalDate.now();
                int a = anio > 0 ? anio : hoy.getYear();
                int m = mes  > 0 ? mes  : hoy.getMonthValue();
                LocalDate primero = LocalDate.of(a, m, 1);
                LocalDate ultimo  = primero.withDayOfMonth(primero.lengthOfMonth());
                inicio = primero.atStartOfDay();
                fin    = ultimo.atTime(LocalTime.MAX);
                periodo = primero.format(DateTimeFormatter.ofPattern("MMMM yyyy",
                        new java.util.Locale("es", "CO")));
            }
        }

        List<Pedido> pedidos = pedidoRepo.findByRangoFechas(inicio, fin).stream()
                .filter(p -> p.getEstado() != Pedido.Estado.CANCELADO)
                .collect(Collectors.toList());

        List<Factura> facturas = facturaRepo.findByFechaEmisionBetween(inicio, fin);

        LocalDate desdeFecha = inicio.toLocalDate();
        LocalDate hastaFecha = fin.toLocalDate();
        List<Mantenimiento> mantenimientos = mantenimientoRepo
                .findByFechaBetweenOrderByFechaAscHoraInicioAsc(desdeFecha, hastaFecha).stream()
                .filter(m -> m.getEstado() == Mantenimiento.Estado.FINALIZADO)
                .collect(Collectors.toList());

        BigDecimal totalWeb = pedidos.stream()
                .map(p -> p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPresencial = facturas.stream()
                .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMant = mantenimientos.stream()
                .map(m -> m.getCostoTotal() != null ? m.getCostoTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGeneral = totalWeb.add(totalPresencial).add(totalMant);

        BigDecimal ticketPromedio = pedidos.isEmpty() ? BigDecimal.ZERO
                : totalWeb.divide(BigDecimal.valueOf(pedidos.size()), 0,
                        java.math.RoundingMode.HALF_UP);

        return new DatosReporte(periodo, pedidos, facturas, mantenimientos,
                totalGeneral, totalWeb, totalPresencial, totalMant, ticketPromedio);
    }

    private String fmt(BigDecimal v) {
        if (v == null) return "$0";
        return "$ " + String.format("%,.0f", v);
    }

    record DatosReporte(
            String periodo,
            List<Pedido> pedidos,
            List<Factura> facturas,
            List<Mantenimiento> mantenimientos,
            BigDecimal totalGeneral,
            BigDecimal totalWeb,
            BigDecimal totalPresencial,
            BigDecimal totalMant,
            BigDecimal ticketPromedio
    ) {}
}
