package com.bikeshop.service;

import com.bikeshop.dto.auth.*;
import com.bikeshop.entity.Cliente;
import com.bikeshop.entity.PasswordResetToken;
import com.bikeshop.entity.Usuario;
import com.bikeshop.repository.ClienteRepository;
import com.bikeshop.repository.PasswordResetTokenRepository;
import com.bikeshop.repository.UsuarioRepository;
import com.bikeshop.security.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepo;
    private final ClienteRepository clienteRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final NotificacionService notificacionService;
    private final PasswordResetTokenRepository resetTokenRepo;

    @Value("${app.base-url}")
    private String appBaseUrl;

    // ─── MENSAJE DE ERROR GENÉRICO (H7) ─────────────────────────────────────
    // Usar siempre el mismo mensaje para "usuario no existe" y "clave incorrecta".
    // Mensajes específicos revelan qué campo falló y permiten enumerar usuarios.
    private static final String MSG_CREDENCIALES = "Credenciales inválidas";

    // ─── LOGIN USUARIO INTERNO (Admin / Empleado) ───────────────────────────

    @Transactional
    public AuthResponse loginUsuario(LoginUsuarioRequest request) {
        // H7: mismo mensaje si no existe o si la clave es incorrecta
        Usuario usuario = usuarioRepo.findByNumeroDocumento(request.getNumeroDocumento())
                .orElseThrow(() -> new BadCredentialsException(MSG_CREDENCIALES));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalStateException("Usuario inactivo. Contacta al administrador.");
        }

        if (!passwordEncoder.matches(request.getContrasena(), usuario.getContrasenaHash())) {
            throw new BadCredentialsException(MSG_CREDENCIALES);
        }

        usuario.setUltimoAcceso(java.time.LocalDateTime.now());
        usuarioRepo.save(usuario);

        String token = tokenProvider.generateToken(usuario.getId(), "USUARIO", usuario.getRol().name());
        String refreshToken = tokenProvider.generateRefreshToken(usuario.getId(), "USUARIO");

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tipo("USUARIO")
                .rol(usuario.getRol().name())
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .debeCambiarClave(usuario.getDebeCambiarClave())
                .build();
    }

    // ─── LOGIN CLIENTE ───────────────────────────────────────────────────────

    public AuthResponse loginCliente(LoginClienteRequest request) {
        // H7: mismo mensaje si no existe o si la clave es incorrecta
        Cliente cliente = clienteRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException(MSG_CREDENCIALES));

        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new IllegalStateException("Cuenta desactivada. Contacta al administrador.");
        }

        if (cliente.getContrasenaHash() == null) {
            throw new IllegalStateException("Esta cuenta fue creada como invitado. Usa el flujo de registro.");
        }

        if (!passwordEncoder.matches(request.getContrasena(), cliente.getContrasenaHash())) {
            throw new BadCredentialsException(MSG_CREDENCIALES);
        }

        String token = tokenProvider.generateToken(cliente.getId(), "CLIENTE", "CLIENTE");
        String refreshToken = tokenProvider.generateRefreshToken(cliente.getId(), "CLIENTE");

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tipo("CLIENTE")
                .rol("CLIENTE")
                .id(cliente.getId())
                .nombre(cliente.getNombreCompleto())
                .email(cliente.getEmail())
                .celular(cliente.getCelular())
                .debeCambiarClave(Boolean.TRUE.equals(cliente.getDebeCambiarClave()))
                .build();
    }

    // ─── REGISTRO CLIENTE ────────────────────────────────────────────────────

    @Transactional
    public AuthResponse registroCliente(RegistroClienteRequest request) {
        if (clienteRepo.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese email.");
        }

        Cliente cliente = Cliente.builder()
                .nombreCompleto(request.getNombreCompleto())
                .email(request.getEmail())
                .contrasenaHash(passwordEncoder.encode(request.getContrasena()))
                .celular(request.getCelular())
                .direccion(request.getDireccion())
                .ciudad(request.getCiudad())
                .build();

        cliente = clienteRepo.save(cliente);

        String token = tokenProvider.generateToken(cliente.getId(), "CLIENTE", "CLIENTE");
        String refreshToken = tokenProvider.generateRefreshToken(cliente.getId(), "CLIENTE");

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tipo("CLIENTE")
                .rol("CLIENTE")
                .id(cliente.getId())
                .nombre(cliente.getNombreCompleto())
                .email(cliente.getEmail())
                .celular(cliente.getCelular())
                .build();
    }

    // ─── CAMBIAR CLAVE USUARIO INTERNO ───────────────────────────────────────

    @Transactional
    public void cambiarClaveUsuario(Long usuarioId, CambiarClaveRequest request) {
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getClaveActual(), usuario.getContrasenaHash())) {
            throw new BadCredentialsException("La clave actual es incorrecta");
        }

        usuario.setContrasenaHash(passwordEncoder.encode(request.getClaveNueva()));
        usuario.setDebeCambiarClave(false);
        usuarioRepo.save(usuario);
    }

    // ─── RECUPERAR CLAVE CLIENTE — envía enlace firmado (H8) ─────────────────

    /**
     * Genera un token de reset de un solo uso y lo envía como enlace por email.
     *
     * H7: siempre responde con éxito aunque el email no exista
     *     (para no revelar qué emails están registrados).
     * H8: ya no se envía contraseña temporal en texto plano.
     *     El token expira en 1 hora y es de un solo uso.
     */
    @Transactional
    public void recuperarClaveCliente(RecuperarClaveRequest request) {
        Optional<Cliente> clienteOpt = clienteRepo.findByEmail(request.getEmail());

        // H7: si el email no existe, salimos silenciosamente (misma respuesta que si sí existe)
        if (clienteOpt.isEmpty()) {
            return;
        }

        Cliente cliente = clienteOpt.get();

        // Invalidar tokens anteriores de este cliente
        resetTokenRepo.deleteByClienteId(cliente.getId());

        // Generar token en claro y almacenar su hash SHA-256
        String plainToken = generarTokenReset();
        String tokenHash = hashToken(plainToken);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .tokenHash(tokenHash)
                .clienteId(cliente.getId())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        resetTokenRepo.save(resetToken);

        // Enviar enlace por email — el token en claro va en la URL, nunca el hash
        String enlace = appBaseUrl + "/reset-password?token=" + plainToken;
        notificacionService.enviarEmailSimple(
                cliente.getEmail(),
                "Recuperación de contraseña - BikeShop",
                "Hola " + cliente.getNombreCompleto() + ",\n\n" +
                "Recibimos una solicitud para restablecer la contraseña de tu cuenta.\n\n" +
                "Haz clic en el siguiente enlace para crear una nueva contraseña:\n\n" +
                enlace + "\n\n" +
                "Este enlace es válido por 1 hora y solo puede usarse una vez.\n\n" +
                "Si no solicitaste este cambio, ignora este mensaje.\n" +
                "Tu contraseña actual seguirá siendo la misma.\n\n" +
                "— BikeShop"
        );
    }

    // ─── APLICAR RESET DE CLAVE (H8) ─────────────────────────────────────────

    @Transactional
    public void resetClaveCliente(ResetClaveRequest request) {
        String tokenHash = hashToken(request.getToken());

        PasswordResetToken resetToken = resetTokenRepo.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El enlace es inválido. Solicita uno nuevo."));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Este enlace ya fue utilizado. Solicita uno nuevo.");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El enlace ha expirado. Solicita uno nuevo.");
        }

        Cliente cliente = clienteRepo.findById(resetToken.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));

        cliente.setContrasenaHash(passwordEncoder.encode(request.getClaveNueva()));
        cliente.setDebeCambiarClave(false);
        clienteRepo.save(cliente);

        // Marcar como usado (un solo uso)
        resetToken.setUsed(true);
        resetTokenRepo.save(resetToken);
    }

    // ─── CAMBIAR CLAVE CLIENTE AUTENTICADO ───────────────────────────────────

    @Transactional
    public void cambiarClaveCliente(Long clienteId, CambiarClaveRequest request) {
        Cliente cliente = clienteRepo.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));

        if (!passwordEncoder.matches(request.getClaveActual(), cliente.getContrasenaHash())) {
            throw new BadCredentialsException("La clave actual es incorrecta");
        }

        cliente.setContrasenaHash(passwordEncoder.encode(request.getClaveNueva()));
        cliente.setDebeCambiarClave(false);
        clienteRepo.save(cliente);
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private static final SecureRandom RNG = new SecureRandom();

    /** Genera un token de 32 bytes aleatorios codificado en hex (64 chars). */
    private String generarTokenReset() {
        byte[] bytes = new byte[32];
        RNG.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    /** SHA-256 del token en claro. Solo el hash se almacena en BD. */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
