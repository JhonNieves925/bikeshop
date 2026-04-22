package com.bikeshop.dto.empleado;

import com.bikeshop.entity.EmpleadoJornada;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class JornadaResponse {
    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private LocalDate fecha;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private BigDecimal horasTrabajadas;
    private String observaciones;

    public static JornadaResponse from(EmpleadoJornada j) {
        return JornadaResponse.builder()
                .id(j.getId())
                .usuarioId(j.getUsuario().getId())
                .usuarioNombre(j.getUsuario().getNombre())
                .fecha(j.getFecha())
                .horaEntrada(j.getHoraEntrada())
                .horaSalida(j.getHoraSalida())
                .horasTrabajadas(j.getHorasTrabajadas())
                .observaciones(j.getObservaciones())
                .build();
    }
}
