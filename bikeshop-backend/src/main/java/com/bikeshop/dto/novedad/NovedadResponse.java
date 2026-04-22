package com.bikeshop.dto.novedad;

import com.bikeshop.entity.Novedad;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class NovedadResponse {

    private Long id;
    private String titulo;
    private String resumen;
    private String contenido;
    private String imagenUrl;
    private String tipo;
    private boolean activa;
    private boolean destacada;
    private LocalDateTime creadoEn;
    /** URLs de la galería de fotos ordenadas */
    private List<GaleriaItem> galeria;

    @Data
    @Builder
    public static class GaleriaItem {
        private Long id;
        private String imagenUrl;
        private int orden;
    }

    public static NovedadResponse from(Novedad n) {
        List<GaleriaItem> items = n.getGaleria() == null ? List.of() :
                n.getGaleria().stream()
                        .map(img -> GaleriaItem.builder()
                                .id(img.getId())
                                .imagenUrl(img.getImagenUrl())
                                .orden(img.getOrden())
                                .build())
                        .toList();

        return NovedadResponse.builder()
                .id(n.getId())
                .titulo(n.getTitulo())
                .resumen(n.getResumen())
                .contenido(n.getContenido())
                .imagenUrl(n.getImagenUrl())
                .tipo(n.getTipo())
                .activa(Boolean.TRUE.equals(n.getActiva()))
                .destacada(Boolean.TRUE.equals(n.getDestacada()))
                .creadoEn(n.getCreadoEn())
                .galeria(items)
                .build();
    }
}
