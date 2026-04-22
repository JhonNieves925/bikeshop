package com.bikeshop.dto.producto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import com.bikeshop.entity.Producto;
import com.bikeshop.entity.ProductoImagen;
import com.bikeshop.entity.ProdutoTalla;

@Data
@Builder
public class ProductoResumenResponse {
    private Long id;
    private String nombre;
    private BigDecimal precio;
    private int stock;
    private Boolean activo;
    private String imagenPrincipalUrl;
    private Long categoriaId;
    private String categoriaNombre;
    private boolean tieneTallas;

    public static ProductoResumenResponse from(Producto p,
                                               List<ProductoImagen> imagenes,
                                               List<ProdutoTalla> tallas) {
        String urlPrincipal = imagenes.stream()
                .filter(i -> Boolean.TRUE.equals(i.getEsPrincipal()))
                .findFirst()
                .or(() -> imagenes.stream().findFirst())
                .map(ProductoImagen::getUrl)
                .orElse(null);

        return ProductoResumenResponse.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .precio(p.getPrecio())
                .stock(p.getStock())
                .activo(p.getActivo())
                .imagenPrincipalUrl(urlPrincipal)
                .categoriaId(p.getCategoria() != null ? p.getCategoria().getId() : null)
                .categoriaNombre(p.getCategoria() != null ? p.getCategoria().getNombre() : null)
                .tieneTallas(tallas != null && !tallas.isEmpty())
                .build();
    }
}
