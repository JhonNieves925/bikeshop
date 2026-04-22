package com.bikeshop.dto.mantenimiento;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;

@Data
@AllArgsConstructor
public class SlotDisponibleResponse {
    private LocalTime hora;
    private boolean disponible;
}
