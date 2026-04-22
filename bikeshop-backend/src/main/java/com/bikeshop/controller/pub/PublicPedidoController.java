package com.bikeshop.controller.pub;

import com.bikeshop.dto.ApiResponse;
import com.bikeshop.dto.pedido.CrearPedidoRequest;
import com.bikeshop.dto.pedido.PedidoResponse;
import com.bikeshop.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/pedidos")
@RequiredArgsConstructor
public class PublicPedidoController {

    private final PedidoService pedidoService;

    /**
     * Compra sin cuenta — no requiere login.
     * El cliente llena sus datos directamente en el checkout.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PedidoResponse>> crearPedidoInvitado(
            @Valid @RequestBody CrearPedidoRequest request) {
        PedidoResponse pedido = pedidoService.crearPedidoInvitado(request);
        return ResponseEntity.ok(ApiResponse.ok("Pedido creado exitosamente", pedido));
    }
}
