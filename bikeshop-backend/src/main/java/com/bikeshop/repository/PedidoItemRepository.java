package com.bikeshop.repository;

import com.bikeshop.entity.PedidoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PedidoItemRepository extends JpaRepository<PedidoItem, Long> {

    List<PedidoItem> findByPedidoId(Long pedidoId);

    @Modifying
    @Query("UPDATE PedidoItem i SET i.producto = null WHERE i.producto.id = :id")
    void clearProductoByProductoId(@Param("id") Long id);
}
