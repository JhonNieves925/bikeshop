package com.bikeshop.dto.categoria;

import com.bikeshop.entity.Categoria;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoriaResponse {
    private Long id;
    private String nombre;
    private String slug;
    private String descripcion;
    private String iconoUrl;
    private int orden;

    public static CategoriaResponse from(Categoria c) {
        return CategoriaResponse.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .slug(c.getSlug())
                .descripcion(c.getDescripcion())
                .iconoUrl(c.getIconoUrl())
                .orden(c.getOrden())
                .build();
    }
}
