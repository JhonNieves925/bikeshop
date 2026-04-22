package com.bikeshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "factura_items")
public class FacturaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false,
            foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (factura_id) REFERENCES facturas(id) ON DELETE CASCADE"))
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id",
            foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE SET NULL"))
    private Producto producto;

    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column
    @Builder.Default
    private Integer cantidad = 1;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
