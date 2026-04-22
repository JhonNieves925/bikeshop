package com.bikeshop.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && tokenProvider.validateToken(token)) {

            // ── H1: Rechazar refresh tokens usados como access tokens ──────────
            // El claim "typ"="refresh" identifica tokens de refresco.
            // Ningún endpoint protegido debe aceptarlos como acceso.
            if (tokenProvider.isRefreshToken(token)) {
                chain.doFilter(request, response);
                return;
            }

            Long id = tokenProvider.getIdFromToken(token);
            String tipo = tokenProvider.getTipoFromToken(token);
            String rol = tokenProvider.getRolFromToken(token);

            UserPrincipal principal = new UserPrincipal(id, tipo, rol != null ? rol : "");
            String authority = "ROLE_" + (rol != null ? rol : tipo);
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT con prioridad:
     * 1. Cookie HttpOnly "accessToken" (más seguro — no accesible desde JS)
     * 2. Header "Authorization: Bearer ..." (compatible con Postman, apps móviles)
     */
    private String extractToken(HttpServletRequest request) {
        // 1. Buscar en cookies HttpOnly
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    String val = cookie.getValue();
                    if (StringUtils.hasText(val)) {
                        return val;
                    }
                }
            }
        }
        // 2. Fallback: Authorization header (API clients, Postman, apps móviles)
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
