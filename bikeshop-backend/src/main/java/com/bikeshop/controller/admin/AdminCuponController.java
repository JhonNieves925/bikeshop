package com.bikeshop.controller.admin;

import com.bikeshop.dto.ApiResponse;
import com.bikeshop.dto.cupon.CuponRequest;
import com.bikeshop.dto.cupon.CuponResponse;
import com.bikeshop.service.CuponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cupones")
@RequiredArgsConstructor
public class AdminCuponController {

    private final CuponService cuponService;

    /** Empleados y admins pueden consultar cupones (ej: para validar en caja) */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CuponResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(cuponService.listar()));
    }

    /** Solo ADMIN puede crear cupones */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CuponResponse>> crear(@Valid @RequestBody CuponRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Cupón creado", cuponService.crear(req)));
    }

    /** Solo ADMIN puede activar/desactivar cupones */
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CuponResponse>> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", cuponService.toggleActivo(id)));
    }

    /** Solo ADMIN puede eliminar cupones */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        cuponService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Cupón eliminado"));
    }
}
