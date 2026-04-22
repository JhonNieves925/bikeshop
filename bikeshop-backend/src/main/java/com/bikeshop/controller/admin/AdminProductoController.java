package com.bikeshop.controller.admin;

import com.bikeshop.dto.ApiResponse;
import com.bikeshop.dto.producto.ProductoDetalleResponse;
import com.bikeshop.dto.producto.ProductoImagenResponse;
import com.bikeshop.dto.producto.ProductoRequest;
import com.bikeshop.dto.producto.ProductoResumenResponse;
import com.bikeshop.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/productos")
@RequiredArgsConstructor
public class AdminProductoController {

    private final ProductoService productoService;

    /** Empleados y admins pueden listar productos */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductoResumenResponse>>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        return ResponseEntity.ok(ApiResponse.ok(productoService.listarTodosAdmin(pageable)));
    }

    /** Empleados y admins pueden ver detalle de un producto */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoDetalleResponse>> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(productoService.buscarPorIdAdmin(id)));
    }

    /** Solo ADMIN puede crear productos */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductoDetalleResponse>> crear(
            @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Producto creado", productoService.crear(request)));
    }

    /** Solo ADMIN puede editar productos */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductoDetalleResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Producto actualizado", productoService.actualizar(id, request)));
    }

    /** Solo ADMIN puede activar/desactivar productos */
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggle(@PathVariable Long id) {
        productoService.toggleActivo(id);
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado"));
    }

    /** Solo ADMIN puede eliminar productos */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Producto eliminado"));
    }

    // ─── IMÁGENES ───────────────────────────────────────────────────────────

    /** Solo ADMIN puede subir imágenes de productos */
    @PostMapping("/{id}/imagenes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductoImagenResponse>> agregarImagen(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(defaultValue = "false") boolean esPrincipal) {
        ProductoImagenResponse img = productoService.agregarImagen(id, archivo, esPrincipal);
        return ResponseEntity.ok(ApiResponse.ok("Imagen agregada", img));
    }

    /** Solo ADMIN puede cambiar la imagen principal */
    @PatchMapping("/{id}/imagenes/{imagenId}/principal")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductoImagenResponse>> marcarPrincipal(
            @PathVariable Long id,
            @PathVariable Long imagenId) {
        ProductoImagenResponse img = productoService.marcarPrincipal(id, imagenId);
        return ResponseEntity.ok(ApiResponse.ok("Imagen marcada como principal", img));
    }

    /** Solo ADMIN puede eliminar imágenes de productos */
    @DeleteMapping("/{id}/imagenes/{imagenId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminarImagen(
            @PathVariable Long id,
            @PathVariable Long imagenId) {
        productoService.eliminarImagen(id, imagenId);
        return ResponseEntity.ok(ApiResponse.ok("Imagen eliminada"));
    }

    // ─── ALERTAS DE STOCK ───────────────────────────────────────────────────

    /** Empleados y admins pueden ver alertas de stock bajo */
    @GetMapping("/stock-bajo")
    public ResponseEntity<ApiResponse<List<ProductoResumenResponse>>> stockBajo() {
        return ResponseEntity.ok(ApiResponse.ok(productoService.listarConStockBajo()));
    }
}
