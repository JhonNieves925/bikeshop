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
@Table(name = "empleado_actividades")
public class EmpleadoActividad {

    public enum TipoActividad {
        MANTENIMIENTO, VENTA_PRESENCIAL, DESPACHO_WEB, OTRO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_actividad", nullable = false, length = 25)
    private TipoActividad tipoActividad;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(name = "valor_actividad", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorActividad = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por")
    private Usuario registradoPor;

    @CreationTimestamp
    @Column(name = "registrado_en", updatable = false)
    private LocalDateTime registradoEn;
}
