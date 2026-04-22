package com.bikeshop.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegistroClienteRequest {

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    /**
     * H5: Contraseña mínimo 8 caracteres con al menos una letra y un número.
     * Aumentado de 6 a 8 y añadido requisito de complejidad mínima.
     */
    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$",
        message = "La contraseña debe tener al menos 8 caracteres, incluyendo letras y números"
    )
    private String contrasena;

    @NotBlank(message = "El celular es obligatorio")
    private String celular;

    private String direccion;
    private String ciudad;
}
