package com.bikeshop.dto.factura;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FacturaItemRequest {

    // Si viene de un producto en inventario se llena productoId
    private Long productoId;

    // Descripción libre (para servicios manuales como "Revisada básica")
    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @Min(value = 1, message = "La cantidad mínima es 1")
    private int cantidad = 1;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private BigDecimal precioUnitario;
}
