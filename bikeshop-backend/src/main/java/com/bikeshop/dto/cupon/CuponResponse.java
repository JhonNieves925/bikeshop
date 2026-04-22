package com.bikeshop.dto.cupon;

import com.bikeshop.entity.Cupon;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CuponResponse {
    private Long id;
    private String codigo;
    private Cupon.Tipo tipo;
    private BigDecimal descuento;
    private LocalDate fechaExpira;
    private Integer usosMax;
    private int usosActuales;
    private boolean activo;
    private LocalDateTime creadoEn;

    public static CuponResponse from(Cupon c) {
        return CuponResponse.builder()
                .id(c.getId())
                .codigo(c.getCodigo())
                .tipo(c.getTipo())
                .descuento(c.getDescuento())
                .fechaExpira(c.getFechaExpira())
                .usosMax(c.getUsosMax())
                .usosActuales(c.getUsosActuales())
                .activo(c.isActivo())
                .creadoEn(c.getCreadoEn())
                .build();
    }
}
