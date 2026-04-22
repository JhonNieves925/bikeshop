package com.bikeshop.dto.factura;

import com.bikeshop.entity.Factura;
import com.bikeshop.entity.FacturaItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class FacturaResponse {
    private Long id;
    private String nombreCliente;
    private String emailCliente;
    private String tipo;
    private String canal;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal total;
    private String observaciones;
    private String emitidaPorNombre;
    private LocalDateTime fechaEmision;
    private List<FacturaItemResponse> items;

    public static FacturaResponse from(Factura f, List<FacturaItem> items) {
        return FacturaResponse.builder()
                .id(f.getId())
                .nombreCliente(f.getNombreCliente())
                .emailCliente(f.getEmailCliente())
                .tipo(f.getTipo().name())
                .canal(f.getCanal().name())
                .subtotal(f.getSubtotal())
                .descuento(f.getDescuento())
                .total(f.getTotal())
                .observaciones(f.getObservaciones())
                .emitidaPorNombre(f.getEmitidaPor() != null ? f.getEmitidaPor().getNombre() : null)
                .fechaEmision(f.getFechaEmision())
                .items(items.stream().map(FacturaItemResponse::from).toList())
                .build();
    }
}
