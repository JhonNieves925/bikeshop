package com.bikeshop.dto.producto;

import com.bikeshop.dto.produto.ProductoTallaResponse;
import com.bikeshop.entity.Producto;
import com.bikeshop.entity.ProductoImagen;
import com.bikeshop.entity.ProdutoTalla;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ProductoDetalleResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private int stock;
    private int stockMinimo;
    private Boolean activo;
    private Long categoriaId;
    private String categoriaNombre;
    private String categoriaSlug;
    private List<ProductoImagenResponse> imagenes;
    private boolean tieneTallas;
    private List<ProductoTallaResponse> tallas;

    public static ProductoDetalleResponse from(Producto p,
                                               List<ProductoImagen> imagenes,
                                               List<ProdutoTalla> tallas) {
        return ProductoDetalleResponse.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .precio(p.getPrecio())
                .stock(p.getStock())
                .stockMinimo(p.getStockMinimo())
                .activo(p.getActivo())
                .categoriaId(p.getCategoria() != null ? p.getCategoria().getId() : null)
                .categoriaNombre(p.getCategoria() != null ? p.getCategoria().getNombre() : null)
                .categoriaSlug(p.getCategoria() != null ? p.getCategoria().getSlug() : null)
                .imagenes(imagenes.stream().map(ProductoImagenResponse::from).toList())
                .tieneTallas(tallas != null && !tallas.isEmpty())
                .tallas(tallas != null ? tallas.stream().map(ProductoTallaResponse::from).toList() : List.of())
                .build();
    }
}
