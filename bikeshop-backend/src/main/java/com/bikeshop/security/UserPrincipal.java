package com.bikeshop.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private Long id;
    private String tipo; // "USUARIO" o "CLIENTE"
    private String rol;  // "ADMIN", "EMPLEADO" o "CLIENTE"
}
