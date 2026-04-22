package com.bikeshop.controller.cliente;

import com.bikeshop.dto.ApiResponse;
import com.bikeshop.dto.pedido.CrearPedidoRequest;
import com.bikeshop.dto.pedido.PedidoResponse;
import com.bikeshop.security.UserPrincipal;
import com.bikeshop.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes/pedidos")
@RequiredArgsConstructor
public class ClientePedidoController {

    private final PedidoService pedidoService;

    /** Crear pedido como cliente autenticado */
    @PostMapping
    public ResponseEntity<ApiResponse<PedidoResponse>> crearPedido(
            @Valid @RequestBody CrearPedidoRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        PedidoResponse pedido = pedidoService.crearPedidoCliente(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Pedido creado exitosamente", pedido));
    }

    /** Ver mis pedidos */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> misPedidos(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(pedidoService.misPedidos(principal.getId())));
    }
}
