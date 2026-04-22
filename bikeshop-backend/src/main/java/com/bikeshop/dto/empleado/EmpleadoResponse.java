package com.bikeshop.dto.empleado;

import com.bikeshop.entity.Usuario;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class EmpleadoResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String numeroDocumento;
    private String documento;   // alias de numeroDocumento para el frontend
    private String telefono;
    private String rol;
    private String tipoPago;
    private BigDecimal tarifaHora;
    private BigDecimal tarifaServicio;
    private boolean activo;
    private boolean debeCambiarClave;
    private LocalDateTime ultimoAcceso;
    private LocalDateTime creadoEn;

    public static EmpleadoResponse from(Usuario u) {
        return EmpleadoResponse.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .email(u.getEmail())
                .numeroDocumento(u.getNumeroDocumento())
                .documento(u.getNumeroDocumento())   // alias para el frontend
                .telefono(u.getTelefono())
                .rol(u.getRol().name())
                .tipoPago(u.getTipoPago().name())
                .tarifaHora(u.getTarifaHora())
                .tarifaServicio(u.getTarifaServicio())
                .activo(Boolean.TRUE.equals(u.getActivo()))
                .debeCambiarClave(Boolean.TRUE.equals(u.getDebeCambiarClave()))
                .ultimoAcceso(u.getUltimoAcceso())
                .creadoEn(u.getCreadoEn())
                .build();
    }
}
