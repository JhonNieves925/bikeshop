package com.bikeshop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cupones")
public class Cupon {

    public enum Tipo {
        PORCENTAJE, VALOR_FIJO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Tipo tipo;

    /** Valor del descuento: porcentaje (ej. 15.00) o monto fijo (ej. 20000) */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal descuento;

    /** null = sin fecha límite */
    @Column(name = "fecha_expira")
    private LocalDate fechaExpira;

    /** null = usos ilimitados */
    @Column(name = "usos_max")
    private Integer usosMax;

    @Column(name = "usos_actuales", nullable = false)
    @Builder.Default
    private int usosActuales = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;
}
