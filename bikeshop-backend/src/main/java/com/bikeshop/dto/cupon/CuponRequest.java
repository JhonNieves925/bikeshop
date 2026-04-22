package com.bikeshop.dto.cupon;

import com.bikeshop.entity.Cupon;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CuponRequest {

    @NotBlank(message = "El código es obligatorio")
    @Size(min = 3, max = 30, message = "El código debe tener entre 3 y 30 caracteres")
    private String codigo;

    @NotNull(message = "El tipo es obligatorio")
    private Cupon.Tipo tipo;

    @NotNull(message = "El descuento es obligatorio")
    @DecimalMin(value = "0.01", message = "El descuento debe ser mayor a 0")
    private BigDecimal descuento;

    /** Opcional — si no se envía el cupón no expira */
    private LocalDate fechaExpira;

    /** Opcional — si no se envía es ilimitado */
    @Min(value = 1, message = "Los usos máximos deben ser al menos 1")
    private Integer usosMax;
}
