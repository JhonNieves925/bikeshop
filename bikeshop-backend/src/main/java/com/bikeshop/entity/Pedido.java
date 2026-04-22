package com.bikeshop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "pedidos")
public class Pedido {

    public enum TipoComprador {
        REGISTRADO, INVITADO
    }

    public enum Estado {
        PENDIENTE, CONFIRMADO, EN_PREPARACION, DESPACHADO, ENTREGADO, CANCELADO
    }

    public enum Canal {
        WEB, APP
    }

    public enum EstadoPago {
        PENDIENTE, PAGADO, FALLIDO, REEMBOLSADO
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

    @Column(name = "direccion_entrega", nullable = false, length = 255)
    private String direccionEntrega;

    @Column(name = "ciudad_entrega", nullable = false, length = 80)
    private String ciudadEntrega;
    
    @Column(name = "departamento_entrega", nullable = false, length = 100)
    private String departamentoEntrega;

    @Column(name = "notas_entrega", columnDefinition = "TEXT")
    private String notasEntrega;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprador", length = 20)
    @Builder.Default
    private TipoComprador tipoComprador = TipoComprador.INVITADO;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Estado estado = Estado.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private Canal canal = Canal.WEB;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    /** Código del cupón aplicado (null si no se usó ninguno). */
    @Column(name = "cupon_codigo", length = 50)
    private String cuponCodigo;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "referencia_pago", length = 100)
    private String referenciaPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", length = 20)
    @Builder.Default
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "despachado_por")
    private Usuario despachadoPor;

    @Column(name = "fecha_despacho")
    private LocalDateTime fechaDespacho;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoItem> items;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}
