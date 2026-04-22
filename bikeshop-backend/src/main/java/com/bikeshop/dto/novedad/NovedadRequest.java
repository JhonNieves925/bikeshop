package com.bikeshop.dto.novedad;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NovedadRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150)
    private String titulo;

    private String resumen;

    private String contenido;

    private String imagenUrl;

    private String tipo;

    private boolean activa = true;

    private boolean destacada = false;
}
