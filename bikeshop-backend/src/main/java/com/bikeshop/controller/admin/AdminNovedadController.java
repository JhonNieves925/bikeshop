package com.bikeshop.controller.admin;

import com.bikeshop.dto.ApiResponse;
import com.bikeshop.dto.novedad.NovedadRequest;
import com.bikeshop.dto.novedad.NovedadResponse;
import com.bikeshop.service.NovedadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/novedades")
@RequiredArgsConstructor
public class AdminNovedadController {

    private final NovedadService novedadService;

    /** Empleados y admins pueden ver las novedades publicadas */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NovedadResponse>>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(novedadService.listarTodas(pageable)));
    }

    /** Solo ADMIN puede publicar novedades */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NovedadResponse>> crear(
            @Valid @RequestBody NovedadRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Novedad creada", novedadService.crear(request)));
    }

    /** Solo ADMIN puede editar novedades */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NovedadResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody NovedadRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Novedad actualizada", novedadService.actualizar(id, request)));
    }

    /** Solo ADMIN puede eliminar novedades */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        novedadService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Novedad eliminada"));
    }

    /** Solo ADMIN puede subir portada de novedad */
    @PostMapping("/{id}/imagen")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NovedadResponse>> subirPortada(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(ApiResponse.ok("Portada actualizada", novedadService.subirPortada(id, archivo)));
    }

    // ─── GALERÍA ──────────────────────────────────────────────────────────────

    /** Solo ADMIN puede agregar fotos a la galería */
    @PostMapping("/{id}/galeria")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NovedadResponse>> agregarFotoGaleria(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(ApiResponse.ok("Foto agregada", novedadService.agregarFotoGaleria(id, archivo)));
    }

    /** Solo ADMIN puede eliminar fotos de la galería */
    @DeleteMapping("/{id}/galeria/{imagenId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminarFotoGaleria(
            @PathVariable Long id,
            @PathVariable Long imagenId) {
        novedadService.eliminarFotoGaleria(id, imagenId);
        return ResponseEntity.ok(ApiResponse.ok("Foto eliminada"));
    }
}
