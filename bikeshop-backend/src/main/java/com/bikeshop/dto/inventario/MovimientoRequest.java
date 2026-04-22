package com.bikeshop.dto.inventario;

import com.bikeshop.entity.InventarioMovimiento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MovimientoRequest {

    @NotNull(message = "El producto es obligatorio")
    private Long productoId;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private InventarioMovimiento.TipoMovimiento tipoMovimiento;

    
    private int cantidad;

    private String motivo;
}
