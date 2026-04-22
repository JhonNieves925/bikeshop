package com.bikeshop.controller;

import com.bikeshop.dto.ApiResponse;
import com.bikeshop.dto.auth.*;
import com.bikeshop.security.UserPrincipal;
import com.bikeshop.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.samesite:Lax}")
    private String cookieSameSite;

    // ─── LOGIN ───────────────────────────────────────────────────────────────

    /** Login para Admin y Empleados (por número de documento) */
    @PostMapping("/usuarios/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUsuario(
            @Valid @RequestBody LoginUsuarioRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.loginUsuario(request);
        setAuthCookie(response, authResponse.getToken());
        return ResponseEntity.ok(ApiResponse.ok("Login exitoso", authResponse));
    }

    /** Login para Clientes (por email) */
    @PostMapping("/clientes/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginCliente(
            @Valid @RequestBody LoginClienteRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.loginCliente(request);
        setAuthCookie(response, authResponse.getToken());
        return ResponseEntity.ok(ApiResponse.ok("Login exitoso", authResponse));
    }

    /** Registro de nuevos clientes */
    @PostMapping("/clientes/registro")
    public ResponseEntity<ApiResponse<AuthResponse>> registroCliente(
            @Valid @RequestBody RegistroClienteRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.registroCliente(request);
        setAuthCookie(response, authResponse.getToken());
        return ResponseEntity.ok(ApiResponse.ok("Registro exitoso", authResponse));
    }

    // ─── LOGOUT ─────────────────────────────────────────────────────────────

    /**
     * Cierra la sesión limpiando la cookie HttpOnly del lado del servidor.
     * El frontend también debe limpiar su estado local.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        clearAuthCookie(response);
        return ResponseEntity.ok(ApiResponse.ok("Sesión cerrada"));
    }

    // ─── CAMBIAR CLAVE ───────────────────────────────────────────────────────

    /** Cambiar clave (usuarios internos: admin/empleados) */
    @PostMapping("/usuarios/cambiar-clave")
    public ResponseEntity<ApiResponse<Void>> cambiarClaveUsuario(
            @Valid @RequestBody CambiarClaveRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        authService.cambiarClaveUsuario(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Contraseña actualizada correctamente"));
    }

    /** Cambiar clave de cliente autenticado */
    @PostMapping("/clientes/cambiar-clave")
    public ResponseEntity<ApiResponse<Void>> cambiarClaveCliente(
            @Valid @RequestBody CambiarClaveRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        authService.cambiarClaveCliente(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Contraseña actualizada correctamente"));
    }

    // ─── RECUPERACIÓN DE CLAVE (H8) ──────────────────────────────────────────

    /**
     * Solicita recuperación — envía enlace firmado por email.
     * Siempre responde con éxito (H7: no revela si el email está registrado).
     */
    @PostMapping("/clientes/recuperar-clave")
    public ResponseEntity<ApiResponse<Void>> recuperarClave(
            @Valid @RequestBody RecuperarClaveRequest request) {
        authService.recuperarClaveCliente(request);
        return ResponseEntity.ok(ApiResponse.ok(
                "Si ese email está registrado, recibirás un enlace de recuperación."));
    }

    /**
     * Aplica el reset de contraseña usando el token del enlace.
     * El token expira en 1 hora y solo puede usarse una vez.
     */
    @PostMapping("/clientes/reset-clave")
    public ResponseEntity<ApiResponse<Void>> resetClave(
            @Valid @RequestBody ResetClaveRequest request) {
        authService.resetClaveCliente(request);
        return ResponseEntity.ok(ApiResponse.ok("Contraseña actualizada. Ya puedes iniciar sesión."));
    }

    // ─── HELPERS DE COOKIE (Frontend H1) ─────────────────────────────────────

    /**
     * Establece la cookie HttpOnly con el access token.
     *
     * HttpOnly  → no accesible desde JavaScript (protege contra XSS).
     * SameSite  → Lax en dev, Strict en producción (configurado en application.properties).
     * Secure    → false en dev (HTTP), true en producción (HTTPS).
     * Path=/    → se envía con todas las peticiones al servidor.
     */
    private void setAuthCookie(HttpServletResponse response, String token) {
        long maxAgeSeconds = jwtExpirationMs / 1000;
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite(cookieSameSite)
                .maxAge(maxAgeSeconds)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /** Expira la cookie inmediatamente (maxAge=0). */
    private void clearAuthCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite(cookieSameSite)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
