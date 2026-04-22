package com.bikeshop.dto.venta;

import com.bikeshop.entity.FacturaItem;
import com.bikeshop.entity.PedidoItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class VentaItemResponse {

    private String descripcion;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    /** true = producto del inventario, false = servicio/artículo sin stock */
    private boolean tieneInventario;

    public static VentaItemResponse from(PedidoItem item) {
        boolean tieneProducto = item.getProducto() != null;
        return VentaItemResponse.builder()
                .descripcion(tieneProducto ? item.getProducto().getNombre() : "Producto eliminado")
                .cantidad(item.getCantidad())
                .precioUnitario(item.getPrecioUnitario())
                .subtotal(item.getSubtotal())
                .tieneInventario(tieneProducto)
                .build();
    }

    public static VentaItemResponse from(FacturaItem item) {
        return VentaItemResponse.builder()
                .descripcion(item.getDescripcion())
                .cantidad(item.getCantidad())
                .precioUnitario(item.getPrecioUnitario())
                .subtotal(item.getSubtotal())
                .tieneInventario(item.getProducto() != null)
                .build();
    }
}
