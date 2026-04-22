package com.bikeshop.repository;

import com.bikeshop.entity.Bicicleta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BicicletaRepository extends JpaRepository<Bicicleta, Long> {
    List<Bicicleta> findByClienteIdOrderByCreadoEnDesc(Long clienteId);
}
