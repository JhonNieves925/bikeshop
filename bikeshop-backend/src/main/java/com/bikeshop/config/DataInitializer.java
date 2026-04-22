package com.bikeshop.config;

import com.bikeshop.entity.Usuario;
import com.bikeshop.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";

    @Override
    @Transactional
    public void run(String... args) {
        // Solo crea el admin si no existe NINGÚN usuario con rol ADMIN.
        // Esto evita recrear un admin si ya hay uno real registrado.
        if (usuarioRepo.existsByRol(Usuario.Rol.ADMIN)) {
            return;
        }

        // Generar contraseña aleatoria segura de 16 caracteres
        String claveGenerada = generarClaveAleatoria(16);

        Usuario admin = new Usuario();
        admin.setNombre("Administrador BikeShop");
        admin.setNumeroDocumento("ADMIN-INICIAL");
        admin.setContrasenaHash(passwordEncoder.encode(claveGenerada));
        admin.setRol(Usuario.Rol.ADMIN);
        admin.setTipoPago(Usuario.TipoPago.POR_SERVICIO);
        admin.setTarifaHora(BigDecimal.ZERO);
        admin.setTarifaServicio(BigDecimal.ZERO);
        admin.setActivo(true);
        admin.setDebeCambiarClave(true); // FUERZA cambio de clave en el primer login
        usuarioRepo.save(admin);

        // La clave solo se muestra UNA VEZ en los logs del servidor al arrancar.
        // Cámbiala inmediatamente después del primer login.
        log.warn("╔══════════════════════════════════════════════════════╗");
        log.warn("║         ADMIN INICIAL CREADO — SOLO SE VE UNA VEZ   ║");
        log.warn("║  Documento : ADMIN-INICIAL                           ║");
        log.warn("║  Contraseña: {}                       ║", claveGenerada);
        log.warn("║  ⚠ Cambia la contraseña en el primer login           ║");
        log.warn("╚══════════════════════════════════════════════════════╝");
    }

    private String generarClaveAleatoria(int longitud) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
