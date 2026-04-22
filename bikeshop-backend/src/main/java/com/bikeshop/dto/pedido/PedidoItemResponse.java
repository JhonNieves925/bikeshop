package com.bikeshop.dto.pedido;

import com.bikeshop.entity.PedidoItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PedidoItemResponse {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public static PedidoItemResponse from(PedidoItem item) {
        boolean tieneProducto = item.getProducto() != null;
        return PedidoItemResponse.builder()
                .id(item.getId())
                .productoId(tieneProducto ? item.getProducto().getId() : null)
                .productoNombre(tieneProducto ? item.getProducto().getNombre() : "Producto eliminado")
                .cantidad(item.getCantidad())
                .precioUnitario(item.getPrecioUnitario())
                .subtotal(item.getSubtotal())
                .build();
    }
}
