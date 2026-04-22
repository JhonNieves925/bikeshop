package com.bikeshop.repository;

import com.bikeshop.entity.NovedadImagen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NovedadImagenRepository extends JpaRepository<NovedadImagen, Long> {
    List<NovedadImagen> findByNovedadIdOrderByOrdenAsc(Long novedadId);
}
