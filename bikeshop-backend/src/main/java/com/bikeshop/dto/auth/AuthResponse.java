package com.bikeshop.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String tipo;           // "USUARIO" o "CLIENTE"
    private String rol;            // "ADMIN", "EMPLEADO" o "CLIENTE"
    private Long id;
    private String nombre;
    private String email;
    private String celular;
    private Boolean debeCambiarClave; // solo para usuarios internos
}
