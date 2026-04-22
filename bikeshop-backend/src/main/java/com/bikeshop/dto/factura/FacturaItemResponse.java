package com.bikeshop.dto.factura;

import com.bikeshop.entity.FacturaItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FacturaItemResponse {
    private Long id;
    private Long productoId;
    private String descripcion;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public static FacturaItemResponse from(FacturaItem item) {
        return FacturaItemResponse.builder()
                .id(item.getId())
                .productoId(item.getProducto() != null ? item.getProducto().getId() : null)
                .descripcion(item.getDescripcion())
                .cantidad(item.getCantidad())
                .precioUnitario(item.getPrecioUnitario())
                .subtotal(item.getSubtotal())
                .build();
    }
}
