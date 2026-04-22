package com.bikeshop.repository;

import com.bikeshop.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Page<Producto> findByCategoriaIdAndActivoTrue(Long categoriaId, Pageable pageable);

    boolean existsByCategoriaId(Long categoriaId);

    List<Producto> findByCategoriaId(Long categoriaId);

    Page<Producto> findByActivoTrue(Pageable pageable);

    @Query("""
        SELECT p FROM Producto p
        WHERE p.activo = true
          AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
          AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
          AND (:precioMin IS NULL OR p.precio >= :precioMin)
          AND (:precioMax IS NULL OR p.precio <= :precioMax)
        """)
    Page<Producto> buscarActivos(
            @org.springframework.data.repository.query.Param("nombre") String nombre,
            @org.springframework.data.repository.query.Param("categoriaId") Long categoriaId,
            @org.springframework.data.repository.query.Param("precioMin") BigDecimal precioMin,
            @org.springframework.data.repository.query.Param("precioMax") BigDecimal precioMax,
            Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockMinimo AND p.activo = true")
    List<Producto> findProductosConStockBajo();
}
