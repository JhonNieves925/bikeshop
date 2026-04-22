package com.bikeshop.dto.venta;

import com.bikeshop.entity.Factura;
import com.bikeshop.entity.FacturaItem;
import com.bikeshop.entity.Pedido;
import com.bikeshop.entity.PedidoItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Representación unificada de una venta, sea web (Pedido) o presencial (Factura).
 */
@Data
@Builder
public class VentaDetalleResponse {

    private Long id;
    /** "WEB" o "PRESENCIAL" */
    private String fuente;
    /** Referencia visual: "P-5" para pedidos, "F-3" para facturas */
    private String referencia;

    private String nombreCliente;
    private String emailCliente;
    /** Solo pedidos web */
    private String celularCliente;
    /** Solo pedidos web */
    private String direccionEntrega;
    private String ciudadEntrega;

    private LocalDateTime fecha;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    /** Código del cupón aplicado (solo pedidos web) */
    private String codigoCupon;
    private BigDecimal total;

    /** Estado del pedido web (PENDIENTE, CONFIRMADO…) — null para facturas */
    private String estado;
    /** Tipo de factura presencial (VENTA, SERVICIO, MIXTA) — null para pedidos web */
    private String tipoFactura;
    /** Canal de la factura presencial (PRESENCIAL, WEB…) — null para pedidos web */
    private String canalFactura;

    private List<VentaItemResponse> items;

    // ─── Constructores estáticos ────────────────────────────────────────────

    public static VentaDetalleResponse from(Pedido pedido, List<PedidoItem> items) {
        return VentaDetalleResponse.builder()
                .id(pedido.getId())
                .fuente("WEB")
                .referencia("P-" + pedido.getId())
                .nombreCliente(pedido.getNombreCliente())
                .emailCliente(pedido.getEmailCliente())
                .celularCliente(pedido.getCelularCliente())
                .direccionEntrega(pedido.getDireccionEntrega())
                .ciudadEntrega(pedido.getCiudadEntrega())
                .fecha(pedido.getCreadoEn())
                .subtotal(pedido.getSubtotal())
                .descuento(pedido.getDescuento())
                .codigoCupon(pedido.getCuponCodigo())
                .total(pedido.getTotal())
                .estado(pedido.getEstado() != null ? pedido.getEstado().name() : null)
                .items(items.stream().map(VentaItemResponse::from).toList())
                .build();
    }

    public static VentaDetalleResponse from(Factura factura, List<FacturaItem> items) {
        return VentaDetalleResponse.builder()
                .id(factura.getId())
                .fuente("PRESENCIAL")
                .referencia("F-" + factura.getId())
                .nombreCliente(factura.getNombreCliente())
                .emailCliente(factura.getEmailCliente())
                .fecha(factura.getFechaEmision())
                .subtotal(factura.getSubtotal())
                .descuento(factura.getDescuento())
                .total(factura.getTotal())
                .tipoFactura(factura.getTipo() != null ? factura.getTipo().name() : null)
                .canalFactura(factura.getCanal() != null ? factura.getCanal().name() : null)
                .items(items.stream().map(VentaItemResponse::from).toList())
                .build();
    }
}
