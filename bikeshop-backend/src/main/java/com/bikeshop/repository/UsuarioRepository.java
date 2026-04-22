package com.bikeshop.repository;

import com.bikeshop.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNumeroDocumento(String numeroDocumento);

    boolean existsByNumeroDocumento(String numeroDocumento);

    boolean existsByRol(Usuario.Rol rol);

    boolean existsByEmail(String email);

    List<Usuario> findByRolAndActivoTrue(Usuario.Rol rol);

    List<Usuario> findByActivoTrue();
}
