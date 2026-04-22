package com.bikeshop.service;

import com.bikeshop.dto.venta.VentaDetalleResponse;
import com.bikeshop.dto.venta.VentaItemResponse;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Genera el PDF de una factura individual (pedido web o venta presencial).
 * Usa OpenPDF (lowagie), la misma librería que ReporteExportService.
 */
@Service
@RequiredArgsConstructor
public class FacturaPdfService {

    private final VentaService ventaService;

    private static final Color ROJO       = new Color(0xE3, 0x18, 0x37);
    private static final Color OSCURO     = new Color(0x14, 0x14, 0x14);
    private static final Color GRIS_CLARO = new Color(0xF5, 0xF5, 0xF5);
    private static final Color GRIS_BORDE = new Color(0xE0, 0xE0, 0xE0);
    private static final Color VERDE_DESC = new Color(0x05, 0x96, 0x69);

    public byte[] generarPdfWeb(Long pedidoId) {
        return generar(ventaService.detalleWeb(pedidoId));
    }

    public byte[] generarPdfPresencial(Long facturaId) {
        return generar(ventaService.detallePresencial(facturaId));
    }

    private byte[] generar(VentaDetalleResponse v) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // A5 vertical — tamaño ideal para recibos/facturas
            Document doc = new Document(PageSize.A5, 30, 30, 40, 30);
            PdfWriter.getInstance(doc, out);
            doc.open();

            // ── Fuentes ──────────────────────────────────────────────────────
            Font fMarcaRojo  = new Font(Font.HELVETICA, 20, Font.BOLD,  ROJO);
            Font fMarcaBlanco= new Font(Font.HELVETICA, 20, Font.BOLD,  Color.WHITE);
            Font fTitulo     = new Font(Font.HELVETICA, 11, Font.BOLD,  OSCURO);
            Font fBold       = new Font(Font.HELVETICA,  9, Font.BOLD,  OSCURO);
            Font fNormal     = new Font(Font.HELVETICA,  9, Font.NORMAL,OSCURO);
            Font fGris       = new Font(Font.HELVETICA,  8, Font.NORMAL,new Color(0x80,0x80,0x80));
            Font fBlanco     = new Font(Font.HELVETICA,  9, Font.BOLD,  Color.WHITE);
            Font fTotalValor = new Font(Font.HELVETICA, 13, Font.BOLD,  ROJO);
            Font fDescuento  = new Font(Font.HELVETICA,  9, Font.BOLD,  VERDE_DESC);

            DateTimeFormatter dtfFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dtfHora  = DateTimeFormatter.ofPattern("HH:mm");

            // ── Encabezado: marca + tipo de documento ─────────────────────
            PdfPTable header = new PdfPTable(new float[]{1f, 1f});
            header.setWidthPercentage(100);
            header.setSpacingAfter(12);

            // Celda izquierda: BikeShop + subtítulo
            PdfPCell celdaMarca = new PdfPCell();
            celdaMarca.setBorder(Rectangle.NO_BORDER);
            celdaMarca.setBackgroundColor(OSCURO);
            celdaMarca.setPadding(10);
            Paragraph marca = new Paragraph();
            marca.add(new Chunk("BIKE", fMarcaRojo));
            marca.add(new Chunk("SHOP", fMarcaBlanco));
            celdaMarca.addElement(marca);
            Paragraph slogan = new Paragraph("Tu tienda de confianza", fGris);
            celdaMarca.addElement(slogan);
            header.addCell(celdaMarca);

            // Celda derecha: referencia + fecha
            PdfPCell celdaRef = new PdfPCell();
            celdaRef.setBorder(Rectangle.NO_BORDER);
            celdaRef.setBackgroundColor(ROJO);
            celdaRef.setPadding(10);
            celdaRef.setHorizontalAlignment(Element.ALIGN_RIGHT);

            String tipoDoc = "WEB".equals(v.getFuente()) ? "PEDIDO WEB" : "VENTA PRESENCIAL";
            Font fTipoDoc = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(0xFF, 0xCC, 0xCC));
            Paragraph pTipo = new Paragraph(tipoDoc, fTipoDoc);
            pTipo.setAlignment(Element.ALIGN_RIGHT);
            celdaRef.addElement(pTipo);

            Font fRef = new Font(Font.HELVETICA, 16, Font.BOLD, Color.WHITE);
            Paragraph pRef = new Paragraph(v.getReferencia(), fRef);
            pRef.setAlignment(Element.ALIGN_RIGHT);
            celdaRef.addElement(pRef);

            if (v.getFecha() != null) {
                Paragraph pFecha = new Paragraph(
                        v.getFecha().format(dtfFecha) + "  " + v.getFecha().format(dtfHora),
                        new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(0xFF, 0xCC, 0xCC)));
                pFecha.setAlignment(Element.ALIGN_RIGHT);
                celdaRef.addElement(pFecha);
            }
            header.addCell(celdaRef);
            doc.add(header);

            // ── Datos del cliente ─────────────────────────────────────────
            doc.add(seccionLabel("CLIENTE", fGris));

            PdfPTable tCliente = new PdfPTable(new float[]{1.2f, 2.8f});
            tCliente.setWidthPercentage(100);
            tCliente.setSpacingAfter(10);

            filaInfo(tCliente, "Nombre",    v.getNombreCliente(),    fBold, fNormal);
            if (v.getEmailCliente() != null && !v.getEmailCliente().isBlank())
                filaInfo(tCliente, "Email", v.getEmailCliente(),     fBold, fNormal);
            if (v.getCelularCliente() != null && !v.getCelularCliente().isBlank())
                filaInfo(tCliente, "Celular", v.getCelularCliente(), fBold, fNormal);
            if (v.getDireccionEntrega() != null && !v.getDireccionEntrega().isBlank())
                filaInfo(tCliente, "Dirección", v.getDireccionEntrega()
                        + (v.getCiudadEntrega() != null ? ", " + v.getCiudadEntrega() : ""),
                        fBold, fNormal);
            if (v.getEstado() != null)
                filaInfo(tCliente, "Estado pedido", estadoLabel(v.getEstado()), fBold, fNormal);
            if (v.getTipoFactura() != null)
                filaInfo(tCliente, "Tipo", tipoFacturaLabel(v.getTipoFactura()), fBold, fNormal);

            doc.add(tCliente);

            // ── Tabla de ítems ────────────────────────────────────────────
            doc.add(seccionLabel("DETALLE DE PRODUCTOS / SERVICIOS", fGris));

            PdfPTable tItems = new PdfPTable(new float[]{3.5f, 0.8f, 1.8f, 1.8f});
            tItems.setWidthPercentage(100);
            tItems.setSpacingAfter(8);

            // Encabezados
            for (String col : new String[]{"Descripción", "Cant.", "Precio unit.", "Subtotal"}) {
                PdfPCell c = new PdfPCell(new Phrase(col, fBlanco));
                c.setBackgroundColor(OSCURO);
                c.setPadding(5);
                c.setBorder(Rectangle.NO_BORDER);
                if (!"Descripción".equals(col)) c.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tItems.addCell(c);
            }

            boolean alterno = false;
            for (VentaItemResponse item : v.getItems()) {
                Color bg = alterno ? GRIS_CLARO : Color.WHITE;

                PdfPCell cDesc = celdaItem(item.getDescripcion(), fNormal, bg, Element.ALIGN_LEFT);
                PdfPCell cCant = celdaItem(String.valueOf(item.getCantidad()), fNormal, bg, Element.ALIGN_RIGHT);
                PdfPCell cPU   = celdaItem(fmt(item.getPrecioUnitario()), fNormal, bg, Element.ALIGN_RIGHT);
                PdfPCell cSub  = celdaItem(fmt(item.getSubtotal()), fBold, bg, Element.ALIGN_RIGHT);

                // Artículos sin inventario: texto levemente en gris
                if (!item.isTieneInventario()) {
                    Font fServicio = new Font(Font.HELVETICA, 9, Font.ITALIC, new Color(0x55,0x55,0x55));
                    cDesc = celdaItem(item.getDescripcion() + " ✎", fServicio, bg, Element.ALIGN_LEFT);
                }

                tItems.addCell(cDesc);
                tItems.addCell(cCant);
                tItems.addCell(cPU);
                tItems.addCell(cSub);
                alterno = !alterno;
            }
            doc.add(tItems);

            // ── Totales ───────────────────────────────────────────────────
            PdfPTable tTotales = new PdfPTable(new float[]{2.5f, 1.5f});
            tTotales.setWidthPercentage(60);
            tTotales.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tTotales.setSpacingAfter(14);

            boolean tieneDescuento = v.getDescuento() != null &&
                    v.getDescuento().compareTo(BigDecimal.ZERO) > 0;

            if (tieneDescuento) {
                filaTotal(tTotales, "Subtotal", fmt(v.getSubtotal()), fNormal, fNormal, GRIS_CLARO);
                String etiqDesc = "Descuento";
                if (v.getCodigoCupon() != null && !v.getCodigoCupon().isBlank())
                    etiqDesc = "Cupón " + v.getCodigoCupon();
                filaTotal(tTotales, etiqDesc, "− " + fmt(v.getDescuento()),
                        fDescuento, fDescuento, new Color(0xEC, 0xFD, 0xF5));
            }

            // Fila TOTAL con fondo rojo
            PdfPCell cEtiqTotal = new PdfPCell(new Phrase("TOTAL", fBlanco));
            cEtiqTotal.setBackgroundColor(ROJO);
            cEtiqTotal.setPadding(8);
            cEtiqTotal.setBorder(Rectangle.NO_BORDER);
            cEtiqTotal.setHorizontalAlignment(Element.ALIGN_LEFT);

            PdfPCell cValTotal = new PdfPCell(new Phrase(fmt(v.getTotal()), fTotalValor));
            cValTotal.setBackgroundColor(GRIS_CLARO);
            cValTotal.setPadding(8);
            cValTotal.setBorder(Rectangle.NO_BORDER);
            cValTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            // Reemplazar la fuente del total en blanco para la celda roja
            PdfPCell cEtiq2 = new PdfPCell(new Phrase("TOTAL",
                    new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE)));
            cEtiq2.setBackgroundColor(ROJO);
            cEtiq2.setPadding(8);
            cEtiq2.setBorder(Rectangle.NO_BORDER);
            PdfPCell cVal2 = new PdfPCell(new Phrase(fmt(v.getTotal()),
                    new Font(Font.HELVETICA, 11, Font.BOLD, ROJO)));
            cVal2.setBackgroundColor(GRIS_CLARO);
            cVal2.setPadding(8);
            cVal2.setBorder(Rectangle.NO_BORDER);
            cVal2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tTotales.addCell(cEtiq2);
            tTotales.addCell(cVal2);

            doc.add(tTotales);

            // ── Método de pago / canal ────────────────────────────────────
            String metodoPago = "WEB".equals(v.getFuente())
                    ? "Pago contra entrega"
                    : "Efectivo / Tarjeta (presencial)";
            Paragraph pPago = new Paragraph("Método de pago: " + metodoPago, fGris);
            pPago.setSpacingAfter(16);
            doc.add(pPago);

            // ── Pie ───────────────────────────────────────────────────────
            PdfPTable pie = new PdfPTable(1);
            pie.setWidthPercentage(100);
            PdfPCell cPie = new PdfPCell();
            cPie.setBackgroundColor(OSCURO);
            cPie.setPadding(8);
            cPie.setBorder(Rectangle.NO_BORDER);
            cPie.setHorizontalAlignment(Element.ALIGN_CENTER);
            Font fPie = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(0xA0, 0xA0, 0xA0));
            Paragraph pPie = new Paragraph("¡Gracias por tu compra!  ·  BikeShop", fPie);
            pPie.setAlignment(Element.ALIGN_CENTER);
            cPie.addElement(pPie);
            pie.addCell(cPie);
            doc.add(pie);

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de factura: " + e.getMessage(), e);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Paragraph seccionLabel(String texto, Font font) {
        Font f = new Font(Font.HELVETICA, 7, Font.BOLD, new Color(0xA0, 0xA0, 0xA0));
        Paragraph p = new Paragraph(texto, f);
        p.setSpacingBefore(4);
        p.setSpacingAfter(3);
        return p;
    }

    private void filaInfo(PdfPTable tabla, String label, String valor, Font fLabel, Font fValor) {
        PdfPCell cL = new PdfPCell(new Phrase(label, fLabel));
        cL.setBorder(Rectangle.BOTTOM);
        cL.setBorderColor(GRIS_BORDE);
        cL.setBorderWidth(0.3f);
        cL.setPadding(4);

        PdfPCell cV = new PdfPCell(new Phrase(valor != null ? valor : "—", fValor));
        cV.setBorder(Rectangle.BOTTOM);
        cV.setBorderColor(GRIS_BORDE);
        cV.setBorderWidth(0.3f);
        cV.setPadding(4);

        tabla.addCell(cL);
        tabla.addCell(cV);
    }

    private PdfPCell celdaItem(String texto, Font font, Color bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(texto != null ? texto : "", font));
        c.setBackgroundColor(bg);
        c.setPadding(4);
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(GRIS_BORDE);
        c.setBorderWidth(0.3f);
        c.setHorizontalAlignment(align);
        return c;
    }

    private void filaTotal(PdfPTable tabla, String label, String valor,
                            Font fLabel, Font fValor, Color bg) {
        PdfPCell cL = new PdfPCell(new Phrase(label, fLabel));
        cL.setBackgroundColor(bg);
        cL.setPadding(5);
        cL.setBorder(Rectangle.NO_BORDER);

        PdfPCell cV = new PdfPCell(new Phrase(valor, fValor));
        cV.setBackgroundColor(bg);
        cV.setPadding(5);
        cV.setBorder(Rectangle.NO_BORDER);
        cV.setHorizontalAlignment(Element.ALIGN_RIGHT);

        tabla.addCell(cL);
        tabla.addCell(cV);
    }

    private String fmt(BigDecimal v) {
        if (v == null) return "$ 0";
        return "$ " + String.format("%,.0f", v);
    }

    private String estadoLabel(String estado) {
        return switch (estado) {
            case "PENDIENTE"      -> "Pendiente";
            case "CONFIRMADO"     -> "Confirmado";
            case "EN_PREPARACION" -> "En preparación";
            case "DESPACHADO"     -> "Despachado";
            case "ENTREGADO"      -> "Entregado";
            case "CANCELADO"      -> "Cancelado";
            default -> estado;
        };
    }

    private String tipoFacturaLabel(String tipo) {
        return switch (tipo) {
            case "VENTA"    -> "Venta de productos";
            case "SERVICIO" -> "Servicio";
            case "MIXTA"    -> "Venta mixta";
            default -> tipo;
        };
    }
}
