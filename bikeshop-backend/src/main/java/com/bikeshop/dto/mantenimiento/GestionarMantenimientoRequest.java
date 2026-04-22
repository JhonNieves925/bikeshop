package com.bikeshop.dto.mantenimiento;

import com.bikeshop.entity.Mantenimiento;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Data
public class GestionarMantenimientoRequest {

    @NotNull(message = "El estado es obligatorio")
    private Mantenimiento.Estado estado;

    // Campos que se llenan al iniciar (EN_CURSO)
    private String diagnostico;
    private Long atendidoPorId;

    // Campos que se llenan al finalizar (FINALIZADO)
    private String trabajosRealizados;
    private String notasInternas;
    private BigDecimal costoManoObra;
    private LocalTime horaFinReal;

    // Repuestos usados (se agregan al finalizar)
    private List<MantenimientoRepuestoRequest> repuestos;
}
