package com.bikeshop.repository;

import com.bikeshop.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /** Eliminar todos los tokens anteriores de un cliente antes de crear uno nuevo. */
    void deleteByClienteId(Long clienteId);

    /** Limpieza periódica opcional: borra tokens expirados y usados. */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :ahora OR t.used = true")
    void purgarExpiradosYUsados(@Param("ahora") LocalDateTime ahora);
}
