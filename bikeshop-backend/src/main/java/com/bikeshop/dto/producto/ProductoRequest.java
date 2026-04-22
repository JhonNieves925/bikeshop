package com.bikeshop.dto.producto;

import com.bikeshop.dto.produto.ProductoTallaRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductoRequest {

    private Long categoriaId;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150)
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private int stock = 0;

    @Min(value = 0)
    private int stockMinimo = 5;

    private boolean activo = true;

    /** Tallas opcionales. Si se envían, el stock del producto se calcula como la suma de stocks por talla. */
    @Valid
    private List<ProductoTallaRequest> tallas;
}
