package com.bikeshop.dto.producto;

import com.bikeshop.entity.ProductoImagen;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductoImagenResponse {
    private Long id;
    private String url;
    private boolean esPrincipal;
    private int orden;

    public static ProductoImagenResponse from(ProductoImagen img) {
        return ProductoImagenResponse.builder()
                .id(img.getId())
                .url(img.getUrl())
                .esPrincipal(Boolean.TRUE.equals(img.getEsPrincipal()))
                .orden(img.getOrden())
                .build();
    }
}
