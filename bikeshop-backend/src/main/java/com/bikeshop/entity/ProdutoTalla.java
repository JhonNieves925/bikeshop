package com.bikeshop.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "producto_tallas",
       uniqueConstraints = @UniqueConstraint(columnNames = {"producto_id", "talla"}))
public class ProdutoTalla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Producto producto;

    @Column(nullable = false, length = 20)
    private String talla;

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;
}
