package com.bikeshop.repository;

import com.bikeshop.entity.ProdutoTalla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProdutoTallaRepository extends JpaRepository<ProdutoTalla, Long> {

    @Query("SELECT t FROM ProdutoTalla t WHERE t.producto.id = :id ORDER BY t.id ASC")
    List<ProdutoTalla> findByProductoId(@Param("id") Long id);

    @Modifying
    @Query("DELETE FROM ProdutoTalla t WHERE t.producto.id = :id")
    void deleteByProductoId(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM ProdutoTalla t WHERE t.producto.id = :id")
    boolean existsByProductoId(@Param("id") Long id);
}
