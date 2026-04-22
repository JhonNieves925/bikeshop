package com.bikeshop.controller.admin;

import com.bikeshop.dto.ApiResponse;
import com.bikeshop.dto.categoria.CategoriaRequest;
import com.bikeshop.dto.categoria.CategoriaResponse;
import com.bikeshop.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categorias")
@RequiredArgsConstructor
public class AdminCategoriaController {

    private final CategoriaService categoriaService;

    /** Empleados y admins pueden listar categorías */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(categoriaService.listarTodas()));
    }

    /** Solo ADMIN puede crear categorías */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoriaResponse>> crear(
            @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Categoría creada", categoriaService.crear(request)));
    }

    /** Solo ADMIN puede editar categorías */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoriaResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Categoría actualizada", categoriaService.actualizar(id, request)));
    }

    /** Solo ADMIN puede activar/desactivar categorías */
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggle(@PathVariable Long id) {
        categoriaService.toggleActiva(id);
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado"));
    }

    /** Solo ADMIN puede subir imagen de categoría */
    @PostMapping("/{id}/imagen")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoriaResponse>> subirImagen(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(ApiResponse.ok("Imagen actualizada", categoriaService.subirImagen(id, archivo)));
    }

    /** Solo ADMIN puede eliminar categorías */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Categoría eliminada"));
    }
}
