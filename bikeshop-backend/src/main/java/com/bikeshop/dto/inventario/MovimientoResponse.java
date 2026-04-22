package com.bikeshop.dto.inventario;

import com.bikeshop.entity.InventarioMovimiento;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MovimientoResponse {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private String tipoMovimiento;
    private int cantidad;
    private int stockAnterior;
    private int stockNuevo;
    private String motivo;
    private LocalDateTime fecha;

    public static MovimientoResponse from(InventarioMovimiento m) {
        boolean tieneProducto = m.getProducto() != null;
        return MovimientoResponse.builder()
                .id(m.getId())
                .productoId(tieneProducto ? m.getProducto().getId() : null)
                .productoNombre(tieneProducto ? m.getProducto().getNombre() : "Producto eliminado")
                .tipoMovimiento(m.getTipoMovimiento().name())
                .cantidad(m.getCantidad())
                .stockAnterior(m.getStockAnterior())
                .stockNuevo(m.getStockNuevo())
                .motivo(m.getMotivo())
                .fecha(m.getFecha())
                .build();
    }
}
