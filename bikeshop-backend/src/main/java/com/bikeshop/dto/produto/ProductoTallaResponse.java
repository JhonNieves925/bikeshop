package com.bikeshop.dto.produto;

import com.bikeshop.entity.ProdutoTalla;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductoTallaResponse {
    private Long id;
    private String talla;
    private int stock;

    public static ProductoTallaResponse from(ProdutoTalla t) {
        return ProductoTallaResponse.builder()
                .id(t.getId())
                .talla(t.getTalla())
                .stock(t.getStock())
                .build();
    }
}
