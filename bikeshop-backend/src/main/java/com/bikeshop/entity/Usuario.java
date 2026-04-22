package com.bikeshop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "usuarios")
public class Usuario {

    public enum Rol {
        ADMIN, EMPLEADO
    }

    public enum TipoPago {
        POR_HORA, POR_SERVICIO, MIXTO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 120)
    private String apellido;

    @Column(length = 180, unique = true)
    private String email;

    @Column(name = "numero_documento", nullable = false, unique = true, length = 20)
    private String numeroDocumento;

    @Column(name = "contrasena_hash", nullable = false, length = 255)
    private String contrasenaHash;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Rol rol = Rol.EMPLEADO;

    @Column(length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago", length = 20)
    @Builder.Default
    private TipoPago tipoPago = TipoPago.POR_SERVICIO;

    @Column(name = "tarifa_hora", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal tarifaHora = BigDecimal.ZERO;

    @Column(name = "tarifa_servicio", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal tarifaServicio = BigDecimal.ZERO;

    @Column
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "debe_cambiar_clave")
    @Builder.Default
    private Boolean debeCambiarClave = false;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}
