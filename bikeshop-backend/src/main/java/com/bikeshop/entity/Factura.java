package com.bikeshop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "facturas")
public class Factura {

    public enum Tipo {
        VENTA, SERVICIO, MIXTA
    }

    public enum Canal {
        WEB, PRESENCIAL, MANTENIMIENTO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mantenimiento_id")
    private Mantenimiento mantenimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emitida_por")
    private Usuario emitidaPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(name = "nombre_cliente", nullable = false, length = 120)
    private String nombreCliente;

    @Column(name = "email_cliente", length = 120)
    private String emailCliente;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Tipo tipo = Tipo.VENTA;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Canal canal = Canal.PRESENCIAL;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "fecha_emision", updatable = false)
    private LocalDateTime fechaEmision;
}
