package com.bikeshop.dto.mantenimiento;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DisponibilidadResponse {
    private LocalDate fecha;
    private boolean diaDisponible;
    private String motivo; // si no está disponible: "DIA_BLOQUEADO", "TALLER_CERRADO"
    private List<SlotDisponibleResponse> slots;
}
