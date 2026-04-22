package com.bikeshop.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CambiarClaveRequest {

    @NotBlank(message = "La clave actual es obligatoria")
    private String claveActual;

    /**
     * H5: Nueva contraseña mínimo 8 caracteres con al menos una letra y un número.
     */
    @NotBlank(message = "La nueva clave es obligatoria")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$",
        message = "La contraseña debe tener al menos 8 caracteres, incluyendo letras y números"
    )
    private String claveNueva;
}
