package com.bikeshop.controller.pub;

import com.bikeshop.dto.ApiResponse;
import com.bikeshop.dto.categoria.CategoriaResponse;
import com.bikeshop.dto.cupon.ValidarCuponRequest;
import com.bikeshop.dto.cupon.ValidarCuponResponse;
import com.bikeshop.dto.producto.ProductoDetalleResponse;
import com.bikeshop.dto.producto.ProductoResumenResponse;
import com.bikeshop.service.CategoriaService;
import com.bikeshop.service.CuponService;
import com.bikeshop.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicCatalogoController {

    private final CategoriaService categoriaService;
    private final ProductoService productoService;
    private final CuponService cuponService;

    // ─── CATEGORÍAS ─────────────────────────────────────────────────────────

    /** Lista todas las categorías activas (para el menú de la tienda) */
    @GetMapping("/categorias")
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> getCategorias() {
        return ResponseEntity.ok(ApiResponse.ok(categoriaService.listarActivas()));
    }

    /** Detalle de una categoría por slug */
    @GetMapping("/categorias/{slug}")
    public ResponseEntity<ApiResponse<CategoriaResponse>> getCategoria(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(categoriaService.buscarPorSlug(slug)));
    }

    // ─── PRODUCTOS ──────────────────────────────────────────────────────────

    /**
     * Lista productos activos con paginación.
     * Parámetros opcionales: q (búsqueda por nombre), categoriaId, precioMin, precioMax,
     *                        page (0-based), size, sort (nombre | precio)
     */
    @GetMapping("/productos")
    public ResponseEntity<ApiResponse<Page<ProductoResumenResponse>>> getProductos(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "nombre") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        Page<ProductoResumenResponse> resultado = productoService.buscarActivos(q, categoriaId, precioMin, precioMax, pageable);
        return ResponseEntity.ok(ApiResponse.ok(resultado));
    }

    /** Detalle completo de un producto con todas sus imágenes */
    @GetMapping("/productos/{id}")
    public ResponseEntity<ApiResponse<ProductoDetalleResponse>> getProducto(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(productoService.buscarPorId(id)));
    }

    // ─── CUPONES ────────────────────────────────────────────────────────────

    /** Valida un cupón y calcula el descuento sobre el subtotal enviado */
    @PostMapping("/cupones/validar")
    public ResponseEntity<ApiResponse<ValidarCuponResponse>> validarCupon(
            @Valid @RequestBody ValidarCuponRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cupón válido", cuponService.validar(request)));
    }
}
