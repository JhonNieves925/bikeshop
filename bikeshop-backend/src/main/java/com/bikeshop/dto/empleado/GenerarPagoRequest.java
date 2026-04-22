package com.bikeshop.dto.empleado;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GenerarPagoRequest {

    @NotNull(message = "El empleado es obligatorio")
    private Long usuarioId;

    @NotNull(message = "La fecha desde es obligatoria")
    private LocalDate fechaDesde;

    @NotNull(message = "La fecha hasta es obligatoria")
    private LocalDate fechaHasta;

    private BigDecimal bonificaciones = BigDecimal.ZERO;
    private BigDecimal deducciones = BigDecimal.ZERO;
    private String observaciones;
}
