package com.bikeshop.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private SecretKey key;

    // ─── H1: Claim "typ" para distinguir access vs refresh ───────────────────
    private static final String TYP_ACCESS  = "access";
    private static final String TYP_REFRESH = "refresh";

    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Genera un access token.
     * Incluye claim "typ"="access" para que el filtro pueda rechazar refresh tokens
     * usados como access tokens.
     */
    public String generateToken(Long id, String tipo, String rol) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(String.valueOf(id))
                .claim("typ", TYP_ACCESS)
                .claim("tipo", tipo)
                .claim("rol", rol)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * Genera un refresh token.
     * Incluye claim "typ"="refresh" — el filtro de autenticación rechazará este
     * token si alguien intenta usarlo como access token en endpoints protegidos.
     */
    public String generateRefreshToken(Long id, String tipo) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
                .subject(String.valueOf(id))
                .claim("typ", TYP_REFRESH)
                .claim("tipo", tipo)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Long getIdFromToken(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public String getTipoFromToken(String token) {
        return parseClaims(token).get("tipo", String.class);
    }

    public String getRolFromToken(String token) {
        return parseClaims(token).get("rol", String.class);
    }

    /** Retorna true si el token es un refresh token (no debe usarse como access). */
    public boolean isRefreshToken(String token) {
        try {
            String typ = parseClaims(token).get("typ", String.class);
            return TYP_REFRESH.equals(typ);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
