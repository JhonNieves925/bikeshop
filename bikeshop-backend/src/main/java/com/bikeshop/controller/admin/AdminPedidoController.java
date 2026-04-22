package com.bikeshop.controller.admin;

import com.bikeshop.dto.ApiResponse;
import com.bikeshop.dto.pedido.ActualizarEstadoPedidoRequest;
import com.bikeshop.dto.pedido.PedidoResponse;
import com.bikeshop.entity.Pedido;
import com.bikeshop.security.UserPrincipal;
import com.bikeshop.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/pedidos")
@RequiredArgsConstructor
public class AdminPedidoController {

    private final PedidoService pedidoService;

    /** Listar todos los pedidos con paginación */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PedidoResponse>>> listar(
            @RequestParam(required = false) Pedido.Estado estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PedidoResponse> resultado = estado != null
                ? pedidoService.listarPorEstado(estado, pageable)
                : pedidoService.listarTodos(pageable);

        return ResponseEntity.ok(ApiResponse.ok(resultado));
    }

    /** Detalle de un pedido */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PedidoResponse>> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(pedidoService.detalle(id)));
    }

    /**
     * Actualizar estado del pedido.
     * Flujo: PENDIENTE → CONFIRMADO → EN_PREPARACION → DESPACHADO → ENTREGADO
     * Cualquier estado → CANCELADO (devuelve stock automáticamente)
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<PedidoResponse>> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoPedidoRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        PedidoResponse pedido = pedidoService.actualizarEstado(id, request.getEstado(), principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", pedido));
    }
}
