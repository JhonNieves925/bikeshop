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
@Table(name = "empleado_pagos")
public class EmpleadoPago {

    public enum Estado {
        PENDIENTE, PAGADO, PARCIAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por", nullable = false)
    private Usuario registradoPor;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDate fechaDesde;

    @Column(name = "fecha_hasta", nullable = false)
    private LocalDate fechaHasta;

    @Column(name = "total_servicios")
    @Builder.Default
    private Integer totalServicios = 0;

    @Column(name = "total_horas", precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal totalHoras = BigDecimal.ZERO;

    @Column(name = "monto_servicios", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montoServicios = BigDecimal.ZERO;

    @Column(name = "monto_horas", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montoHoras = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal bonificaciones = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal deducciones = BigDecimal.ZERO;

    @Column(name = "total_pago", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalPago = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    @Builder.Default
    private Estado estado = Estado.PENDIENTE;

    @Column(name = "detalle_json", columnDefinition = "JSON")
    private String detalleJson;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "pagado_en")
    private LocalDateTime pagadoEn;
}
