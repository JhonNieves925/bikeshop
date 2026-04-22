package com.bikeshop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mantenimientos")
public class Mantenimiento {

    public enum Estado {
        PENDIENTE, EN_CURSO, FINALIZADO, CANCELADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(name = "nombre_cliente", nullable = false, length = 120)
    private String nombreCliente;

    @Column(name = "email_cliente", nullable = false, length = 120)
    private String emailCliente;

    @Column(name = "celular_cliente", nullable = false, length = 20)
    private String celularCliente;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin_real")
    private LocalTime horaFinReal;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Estado estado = Estado.PENDIENTE;

    @Column(name = "tipo_bicicleta", nullable = false, length = 80)
    private String tipoBicicleta;

    @Column(name = "marca_bicicleta", length = 80)
    private String marcaBicicleta;

    @Column(name = "modelo_bicicleta", length = 80)
    private String modeloBicicleta;

    @Column(name = "problema_reportado", nullable = false, columnDefinition = "TEXT")
    private String problemaReportado;

    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @Column(name = "trabajos_realizados", columnDefinition = "TEXT")
    private String trabajosRealizados;

    @Column(name = "costo_mano_obra", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal costoManoObra = BigDecimal.ZERO;

    @Column(name = "costo_repuestos", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal costoRepuestos = BigDecimal.ZERO;

    @Column(name = "costo_total", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal costoTotal = BigDecimal.ZERO;

    @Column(name = "notas_internas", columnDefinition = "TEXT")
    private String notasInternas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bicicleta_id")
    private Bicicleta bicicleta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendido_por")
    private Usuario atendidoPor;

    @OneToMany(mappedBy = "mantenimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MantenimientoRepuesto> repuestos;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}
