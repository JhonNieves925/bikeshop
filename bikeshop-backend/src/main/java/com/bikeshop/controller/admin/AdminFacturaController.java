package com.bikeshop.controller.admin;

import com.bikeshop.dto.ApiResponse;
import com.bikeshop.dto.factura.CrearFacturaRequest;
import com.bikeshop.dto.factura.FacturaResponse;
import com.bikeshop.security.UserPrincipal;
import com.bikeshop.service.FacturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/facturas")
@RequiredArgsConstructor
public class AdminFacturaController {

    private final FacturaService facturaService;

    /**
     * Emitir factura presencial — ventas en tienda, revisadas rápidas,
     * repuestos vendidos de mostrador, o vincular con un mantenimiento.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FacturaResponse>> crear(
            @Valid @RequestBody CrearFacturaRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Factura emitida",
                facturaService.crear(request, principal.getId())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<FacturaResponse>>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) String nombre) {
        return ResponseEntity.ok(ApiResponse.ok(
                facturaService.listar(desde, hasta, nombre, PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FacturaResponse>> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(facturaService.detalle(id)));
    }
}
