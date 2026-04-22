package com.bikeshop.dto.cupon;

import com.bikeshop.entity.Cupon;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ValidarCuponResponse {
    private String codigo;
    private Cupon.Tipo tipo;
    private BigDecimal descuento;       // porcentaje o monto fijo configurado
    private BigDecimal montoDescuento;  // monto real a descontar del subtotal
    private BigDecimal totalConDescuento;
}
