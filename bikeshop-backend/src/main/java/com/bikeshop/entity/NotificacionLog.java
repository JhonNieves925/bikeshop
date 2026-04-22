package com.bikeshop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notificaciones_log")
public class NotificacionLog {

    public enum TipoReferencia {
        PEDIDO, MANTENIMIENTO, RECORDATORIO, PAGO, SISTEMA
    }

    public enum Canal {
        EMAIL, WHATSAPP, PUSH
    }

    public enum Estado {
        PENDIENTE, ENVIADO, FALLIDO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_referencia", nullable = false, length = 20)
    private TipoReferencia tipoReferencia;

    @Column(nullable = false, length = 150)
    private String destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Canal canal;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    @Builder.Default
    private Estado estado = Estado.PENDIENTE;

    @Column(length = 200)
    private String asunto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column
    @Builder.Default
    private Integer intentos = 0;

    @Column(name = "error_detalle", columnDefinition = "TEXT")
    private String errorDetalle;

    @Column(name = "enviado_en")
    private LocalDateTime enviadoEn;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;
}
