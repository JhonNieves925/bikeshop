package com.bikeshop.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetClaveRequest {

    /** Token en claro recibido por email (el backend busca por su hash). */
    @NotBlank(message = "El token es obligatorio")
    private String token;

    /**
     * Nueva contraseña: mínimo 8 caracteres, debe contener letras y números.
     * El mismo requisito que en registro / cambio de clave.
     */
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$",
        message = "La contraseña debe tener al menos 8 caracteres, incluyendo letras y números"
    )
    private String claveNueva;
}
