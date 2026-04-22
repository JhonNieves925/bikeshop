package com.bikeshop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_completo", nullable = false, length = 120)
    private String nombreCompleto;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "contrasena_hash", length = 255)
    private String contrasenaHash;

    @Column(nullable = false, length = 20)
    private String celular;

    @Column(length = 255)
    private String direccion;

    @Column(length = 80)
    private String ciudad;

    @Column
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "email_verificado")
    @Builder.Default
    private Boolean emailVerificado = false;

    @Column(name = "debe_cambiar_clave")
    @Builder.Default
    private Boolean debeCambiarClave = false;

    @Column(name = "token_verificacion", length = 100)
    private String tokenVerificacion;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}
