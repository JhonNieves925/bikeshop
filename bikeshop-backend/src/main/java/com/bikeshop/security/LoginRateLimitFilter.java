package com.bikeshop.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limita los intentos de login por IP para prevenir ataques de fuerza bruta.
 *
 * Regla: máximo 15 intentos en 15 minutos por IP.
 * Si se supera, responde 429 Too Many Requests durante los minutos restantes.
 *
 * H6 fix: resolución de IP robusta contra spoofing de X-Forwarded-For.
 * Cuando el servidor está detrás de un proxy de confianza, el proxy agrega
 * la IP del cliente al lado DERECHO de X-Forwarded-For. Tomar la IP más a la
 * derecha (la añadida por el proxy, no la declarada por el cliente) evita que
 * un atacante evite el rate-limit poniendo una IP falsa como primer elemento.
 *
 * Si getRemoteAddr() es una IP privada/loopback → estamos detrás de un proxy
 * → tomamos la IP del lado derecho de X-Forwarded-For (la del proxy).
 * Si getRemoteAddr() es una IP pública → el cliente llegó directo → la usamos.
 */
@Component
@Order(1)
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int  MAX_INTENTOS = 15;
    private static final long VENTANA_MS   = 15 * 60 * 1000L; // 15 minutos

    private final ConcurrentHashMap<String, Entrada> registro = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        // Solo aplica a endpoints de autenticación con POST
        boolean esAuth = req.getRequestURI().startsWith("/api/auth/")
                && "POST".equalsIgnoreCase(req.getMethod());

        if (!esAuth) {
            chain.doFilter(req, res);
            return;
        }

        String ip = resolverIp(req);
        Entrada entrada = registro.computeIfAbsent(ip, k -> new Entrada());

        synchronized (entrada) {
            long ahora = System.currentTimeMillis();

            // Resetear ventana si ya expiró
            if (ahora - entrada.inicioVentana > VENTANA_MS) {
                entrada.inicioVentana = ahora;
                entrada.intentos.set(0);
            }

            int intentos = entrada.intentos.incrementAndGet();

            if (intentos > MAX_INTENTOS) {
                long msRestantes = VENTANA_MS - (ahora - entrada.inicioVentana);
                long minRestantes = Math.max(1, msRestantes / 60000);

                res.setStatus(429);
                res.setContentType("application/json;charset=UTF-8");
                res.setHeader("Retry-After", String.valueOf(msRestantes / 1000));
                res.getWriter().write(
                    "{\"success\":false,\"message\":\"Demasiados intentos fallidos. " +
                    "Espera " + minRestantes + " minuto(s) antes de intentar de nuevo.\"}"
                );
                return;
            }
        }

        chain.doFilter(req, res);
    }

    /**
     * Resolución de IP robusta contra spoofing (H6).
     *
     * Algoritmo:
     * - Si getRemoteAddr() es IP pública → el cliente llegó directamente → usarla.
     * - Si getRemoteAddr() es IP privada/loopback → hay un proxy delante → tomar
     *   el elemento más a la DERECHA de X-Forwarded-For (la añadida por el proxy
     *   confiable más externo, no la autodeclarada por el cliente que está a la izquierda).
     */
    private String resolverIp(HttpServletRequest req) {
        String remoteAddr = req.getRemoteAddr();

        if (esIpPrivada(remoteAddr)) {
            // Estamos detrás de un proxy de confianza
            String forwarded = req.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                // Tomar el ÚLTIMO elemento (el que agregó el proxy más externo)
                String[] partes = forwarded.split(",");
                return partes[partes.length - 1].trim();
            }
            String realIp = req.getHeader("X-Real-IP");
            if (realIp != null && !realIp.isBlank()) {
                return realIp.trim();
            }
        }

        // IP pública directa o sin cabecera de proxy
        return remoteAddr;
    }

    /**
     * Retorna true si la IP es privada/loopback (indica presencia de proxy).
     * Rangos RFC 1918 + loopback + IPv6 local.
     */
    private boolean esIpPrivada(String ip) {
        if (ip == null) return false;
        return ip.equals("127.0.0.1")
                || ip.equals("0:0:0:0:0:0:0:1")
                || ip.equals("::1")
                || ip.startsWith("10.")
                || ip.startsWith("192.168.")
                || (ip.startsWith("172.") && esRango172(ip));
    }

    private boolean esRango172(String ip) {
        // 172.16.0.0 – 172.31.255.255
        try {
            int segundo = Integer.parseInt(ip.split("\\.")[1]);
            return segundo >= 16 && segundo <= 31;
        } catch (Exception e) {
            return false;
        }
    }

    private static class Entrada {
        long inicioVentana = System.currentTimeMillis();
        final AtomicInteger intentos = new AtomicInteger(0);
    }
}
