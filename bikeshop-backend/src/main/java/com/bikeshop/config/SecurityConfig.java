package com.bikeshop.config;

import com.bikeshop.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configure(http))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // ── Cabeceras de seguridad HTTP ──────────────────────────────────
            .headers(headers -> headers
                // Evita que el navegador adivine el Content-Type (MIME sniffing)
                .contentTypeOptions(co -> {})
                // Evita que la app sea incrustada en iframes de otros dominios (clickjacking)
                .frameOptions(fo -> fo.sameOrigin())
                // Fuerza HTTPS por 1 año — el backend lo activa solo si HSTS_ENABLED=true
                // En local queda apagado, en producción se pone la variable de entorno

                // Limita qué recursos puede cargar el navegador
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "script-src 'self'; " +
                    "style-src 'self' 'unsafe-inline'; " +
                    "img-src 'self' data: blob:; " +
                    "connect-src 'self'; " +
                    "frame-ancestors 'none';"
                ))
                // Controla qué información del referrer se envía
                .referrerPolicy(rp -> rp.policy(
                    org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
                        .ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                // Desactiva la caché en respuestas autenticadas
                .cacheControl(cc -> {})
            )
            .authorizeHttpRequests(auth -> auth
                // Públicos
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                // Solo ADMIN
                .requestMatchers("/api/admin/reportes/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/empleados/**").hasRole("ADMIN")
                // Admin y Empleados
                .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "EMPLEADO")
                // Clientes autenticados
                .requestMatchers("/api/clientes/**").hasRole("CLIENTE")
                .requestMatchers("/api/cliente/**").hasRole("CLIENTE")
                // Resto requiere auth
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
