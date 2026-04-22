package com.bikeshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Token de recuperación de contraseña (H8).
 *
 * Flujo:
 * 1. El cliente solicita recuperar su clave.
 * 2. Se genera un token aleatorio (32 bytes), se guarda su hash SHA-256 en BD.
 * 3. Se envía por email el token en claro dentro de un enlace con tiempo de expiración.
 * 4. El cliente hace clic → el frontend envía el token al backend.
 * 5. El backend busca por hash, verifica expiración y uso, aplica la nueva contraseña.
 *
 * Ventaja sobre contraseña temporal: el token nunca fue una clave real,
 * expira en 1 hora y es de un solo uso. Si el email es interceptado, la
 * ventana de ataque es mínima.
 */
@Entity
@Table(name = "password_reset_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** SHA-256 del token en claro. Nunca guardamos el token real. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    /** ID del cliente al que pertenece este token. */
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    /** El token expira 1 hora después de creado. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** Marca de un solo uso — se activa cuando el cliente aplica el reset. */
    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;
}
