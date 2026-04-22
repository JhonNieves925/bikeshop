package com.bikeshop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "novedades")
public class Novedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    /** Texto breve mostrado en la card */
    @Column(columnDefinition = "TEXT")
    private String resumen;

    /** Contenido completo del artículo (mostrado en el modal) */
    @Column(columnDefinition = "TEXT")
    private String contenido;

    /** Imagen de portada para la card */
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(length = 20)
    private String tipo;

    @Column
    @Builder.Default
    private Boolean activa = true;

    @Column
    @Builder.Default
    private Boolean destacada = false;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    /** Galería de fotos del artículo */
    @OneToMany(mappedBy = "novedad", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orden ASC")
    @Builder.Default
    private List<NovedadImagen> galeria = new ArrayList<>();
}
