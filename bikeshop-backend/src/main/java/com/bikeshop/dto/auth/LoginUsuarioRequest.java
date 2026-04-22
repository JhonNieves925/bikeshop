package com.bikeshop.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginUsuarioRequest {

    @NotBlank(message = "El número de documento es obligatorio")
    private String numeroDocumento;

    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;
}
