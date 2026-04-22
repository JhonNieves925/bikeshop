package com.bikeshop.dto.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoriaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80)
    private String nombre;

    // Opcional: si no se envía, se genera automáticamente desde el nombre
    @Size(max = 80)
    private String slug;

    private String descripcion;
    private String iconoUrl;
    private int orden;
    private boolean activa = true;
}
