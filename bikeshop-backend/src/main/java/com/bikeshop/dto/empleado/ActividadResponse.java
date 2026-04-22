package com.bikeshop.dto.empleado;

import com.bikeshop.entity.EmpleadoActividad;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ActividadResponse {
    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private String tipoActividad;
    private Long referenciaId;
    private String descripcion;
    private BigDecimal valorActividad;
    private LocalDate fecha;
    private LocalDateTime registradoEn;

    public static ActividadResponse from(EmpleadoActividad a) {
        return ActividadResponse.builder()
                .id(a.getId())
                .usuarioId(a.getUsuario().getId())
                .usuarioNombre(a.getUsuario().getNombre())
                .tipoActividad(a.getTipoActividad().name())
                .referenciaId(a.getReferenciaId())
                .descripcion(a.getDescripcion())
                .valorActividad(a.getValorActividad())
                .fecha(a.getFecha())
                .registradoEn(a.getRegistradoEn())
                .build();
    }
}
