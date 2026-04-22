package com.bikeshop.dto.produto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductoTallaRequest {

    @NotBlank(message = "La talla es obligatoria")
    @Size(max = 20)
    private String talla;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private int stock = 0;
}
