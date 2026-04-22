package com.bikeshop.repository;

import com.bikeshop.entity.Novedad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NovedadRepository extends JpaRepository<Novedad, Long> {

    List<Novedad> findByActivaTrueOrderByCreadoEnDesc();

    Page<Novedad> findAllByOrderByCreadoEnDesc(Pageable pageable);
}
