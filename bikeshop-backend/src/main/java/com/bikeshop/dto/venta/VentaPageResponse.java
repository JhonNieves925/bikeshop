package com.bikeshop.dto.venta;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Respuesta paginada de ventas que incluye el total monetario
 * de TODOS los resultados del filtro (no solo la página actual).
 */
@Data
@Builder
public class VentaPageResponse {
    private List<VentaDetalleResponse> content;
    private int  totalPages;
    private long totalElements;
    /** Suma del campo "total" de todas las ventas que coinciden con el filtro */
    private BigDecimal totalImporte;
}
