package com.bikeshop.dto.empleado;

import com.bikeshop.entity.Usuario;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmpleadoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String apellido;

    @NotBlank(message = "El número de documento es obligatorio")
    private String documento;

    private String contrasena;   // clave al crear
    private String clave;        // alias que también acepta
    private String email;
    private String telefono;
    private Usuario.Rol rol = Usuario.Rol.EMPLEADO;
    private Usuario.TipoPago tipoPago = Usuario.TipoPago.POR_SERVICIO;
}