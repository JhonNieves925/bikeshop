package com.bikeshop.repository;

import com.bikeshop.entity.Factura;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FacturaRepository extends JpaRepository<Factura, Long> {

    @Modifying
    @Query("UPDATE Factura f SET f.emitidaPor = null WHERE f.emitidaPor.id = :id")
    void clearEmitidaPorByUsuarioId(@Param("id") Long id);

    Page<Factura> findAllByOrderByFechaEmisionDesc(Pageable pageable);

    List<Factura> findByFechaEmisionBetween(LocalDateTime desde, LocalDateTime hasta);

    @Query("SELECT f FROM Factura f WHERE " +
    	       "(:desde IS NULL OR f.fechaEmision >= :desde) AND " +
    	       "(:hasta IS NULL OR f.fechaEmision <= :hasta) AND " +
    	       "(:nombre IS NULL OR LOWER(f.nombreCliente) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
    	       "ORDER BY f.fechaEmision DESC")
    	Page<Factura> buscar(@Param("desde") LocalDateTime desde,
    	                     @Param("hasta") LocalDateTime hasta,
    	                     @Param("nombre") String nombre,
    	                     Pageable pageable);
}
