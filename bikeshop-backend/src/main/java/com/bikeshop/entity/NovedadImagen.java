package com.bikeshop.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "novedad_imagenes")
public class NovedadImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novedad_id", nullable = false)
    private Novedad novedad;

    @Column(name = "imagen_url", length = 500, nullable = false)
    private String imagenUrl;

    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 0;
}
