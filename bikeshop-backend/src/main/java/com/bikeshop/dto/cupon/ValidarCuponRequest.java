package com.bikeshop.dto.cupon;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ValidarCuponRequest {
    @NotBlank
    private String codigo;
    @NotNull
    private BigDecimal subtotal;
}
