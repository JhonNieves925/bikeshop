package com.bikeshop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "empleado_jornadas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "fecha"}))
public class EmpleadoJornada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_entrada")
    private LocalTime horaEntrada;

    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    @Column(name = "horas_trabajadas", precision = 5, scale = 2)
    private BigDecimal horasTrabajadas;

    @Column(length = 255)
    private String observaciones;

    @CreationTimestamp
    @Column(name = "registrado_en", updatable = false)
    private LocalDateTime registradoEn;
}
