package com.bikeshop.dto.pedido;

import com.bikeshop.entity.Pedido;
import com.bikeshop.entity.PedidoItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PedidoResponse {
    private Long id;
    private String nombreCliente;
    private String emailCliente;
    private String celularCliente;
    private String direccionEntrega;
    private String ciudadEntrega;
    private String departamentoEntrega;
    private String notasEntrega;
    private String tipoComprador;
    private String estado;
    private String canal;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    /** Código del cupón usado (null si no se aplicó ninguno). */
    private String codigoCupon;
    private BigDecimal total;
    private String estadoPago;
    private LocalDateTime creadoEn;
    private List<PedidoItemResponse> items;

    public static PedidoResponse from(Pedido p, List<PedidoItem> items) {
        return PedidoResponse.builder()
                .id(p.getId())
                .nombreCliente(p.getNombreCliente())
                .emailCliente(p.getEmailCliente())
                .celularCliente(p.getCelularCliente())
                .direccionEntrega(p.getDireccionEntrega())
                .ciudadEntrega(p.getCiudadEntrega())
                .departamentoEntrega(p.getDepartamentoEntrega())
                .notasEntrega(p.getNotasEntrega())
                .tipoComprador(p.getTipoComprador().name())
                .estado(p.getEstado().name())
                .canal(p.getCanal().name())
                .subtotal(p.getSubtotal())
                .descuento(p.getDescuento())
                .codigoCupon(p.getCuponCodigo())
                .total(p.getTotal())
                .estadoPago(p.getEstadoPago().name())
                .creadoEn(p.getCreadoEn())
                .items(items.stream().map(PedidoItemResponse::from).toList())
                .build();
    }
}
