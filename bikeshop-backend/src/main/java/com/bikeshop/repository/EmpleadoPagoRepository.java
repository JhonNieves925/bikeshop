package com.bikeshop.repository;

import com.bikeshop.entity.EmpleadoPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmpleadoPagoRepository extends JpaRepository<EmpleadoPago, Long> {

    /**
     * Reasigna registradoPor al propio empleado pagado, para los registros de pago
     * de otros empleados que fueron generados por el usuario que se está eliminando.
     * Así se preserva el historial de pagos de esos empleados.
     */
    @Modifying
    @Query("UPDATE EmpleadoPago p SET p.registradoPor = p.usuario WHERE p.registradoPor.id = :id AND p.usuario.id <> :id")
    void reassignRegistradoPorByUsuarioId(@Param("id") Long id);

    List<EmpleadoPago> findByUsuarioIdOrderByCreadoEnDesc(Long usuarioId);

    Page<EmpleadoPago> findAllByOrderByCreadoEnDesc(Pageable pageable);

    void deleteByUsuarioId(Long usuarioId);
}
