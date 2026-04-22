package com.bikeshop.controller.admin;

import com.bikeshop.dto.ApiResponse;
import com.bikeshop.dto.inventario.MovimientoRequest;
import com.bikeshop.dto.inventario.MovimientoResponse;
import com.bikeshop.security.UserPrincipal;
import com.bikeshop.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventario")
@RequiredArgsConstructor
public class AdminInventarioController {

    private final InventarioService inventarioService;

    /**
     * Registrar movimiento de inventario.
     * Solo ADMIN puede ajustar el stock manualmente para evitar fraudes o errores no autorizados.
     * Tipos: ENTRADA (mercancía nueva), AJUSTE_MANUAL (corrección), DEVOLUCION (cliente)
     */
    @PostMapping("/movimientos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MovimientoResponse>> registrar(
            @Valid @RequestBody MovimientoRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Movimiento registrado",
                inventarioService.registrarMovimiento(request, principal.getId())));
    }

    /** Empleados y admins pueden consultar el historial completo */
    @GetMapping("/movimientos")
    public ResponseEntity<ApiResponse<Page<MovimientoResponse>>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                inventarioService.listarTodos(PageRequest.of(page, size))));
    }

    /** Empleados y admins pueden ver historial de un producto específico */
    @GetMapping("/movimientos/producto/{productoId}")
    public ResponseEntity<ApiResponse<List<MovimientoResponse>>> porProducto(
            @PathVariable Long productoId) {
        return ResponseEntity.ok(ApiResponse.ok(inventarioService.historialProducto(productoId)));
    }
}
