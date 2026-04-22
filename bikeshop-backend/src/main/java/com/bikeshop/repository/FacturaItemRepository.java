package com.bikeshop.repository;

import com.bikeshop.entity.FacturaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FacturaItemRepository extends JpaRepository<FacturaItem, Long> {

    List<FacturaItem> findByFacturaId(Long facturaId);

    @Modifying
    @Query("UPDATE FacturaItem i SET i.producto = null WHERE i.producto.id = :id")
    void clearProductoByProductoId(@Param("id") Long id);
}
