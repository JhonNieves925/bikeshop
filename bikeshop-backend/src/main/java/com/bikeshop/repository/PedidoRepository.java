package com.bikeshop.repository;

import com.bikeshop.entity.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Modifying
    @Query("UPDATE Pedido p SET p.despachadoPor = null WHERE p.despachadoPor.id = :id")
    void clearDespachadoPorByUsuarioId(@Param("id") Long id);

    Page<Pedido> findByEstadoOrderByCreadoEnDesc(Pedido.Estado estado, Pageable pageable);

    List<Pedido> findByClienteIdOrderByCreadoEnDesc(Long clienteId);

    Page<Pedido> findAllByOrderByCreadoEnDesc(Pageable pageable);

    @Query("SELECT p FROM Pedido p WHERE p.creadoEn BETWEEN :desde AND :hasta")
    List<Pedido> findByRangoFechas(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
