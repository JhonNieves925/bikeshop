package com.bikeshop.dto.mantenimiento;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MantenimientoRepuestoRequest {

    @NotNull(message = "El producto es obligatorio")
    private Long productoId;

    @Min(value = 1, message = "La cantidad mínima es 1")
    private int cantidad;
}
