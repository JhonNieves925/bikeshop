package com.bikeshop.dto.empleado;

import com.bikeshop.entity.EmpleadoPago;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PagoResponse {
    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private int totalServicios;
    private BigDecimal totalHoras;
    private BigDecimal montoServicios;
    private BigDecimal montoHoras;
    private BigDecimal bonificaciones;
    private BigDecimal deducciones;
    private BigDecimal totalPago;
    private String estado;
    private String observaciones;
    private LocalDateTime creadoEn;
    private LocalDateTime pagadoEn;

    public static PagoResponse from(EmpleadoPago p) {
        return PagoResponse.builder()
                .id(p.getId())
                .usuarioId(p.getUsuario().getId())
                .usuarioNombre(p.getUsuario().getNombre())
                .fechaDesde(p.getFechaDesde())
                .fechaHasta(p.getFechaHasta())
                .totalServicios(p.getTotalServicios())
                .totalHoras(p.getTotalHoras())
                .montoServicios(p.getMontoServicios())
                .montoHoras(p.getMontoHoras())
                .bonificaciones(p.getBonificaciones())
                .deducciones(p.getDeducciones())
                .totalPago(p.getTotalPago())
                .estado(p.getEstado().name())
                .observaciones(p.getObservaciones())
                .creadoEn(p.getCreadoEn())
                .pagadoEn(p.getPagadoEn())
                .build();
    }
}
