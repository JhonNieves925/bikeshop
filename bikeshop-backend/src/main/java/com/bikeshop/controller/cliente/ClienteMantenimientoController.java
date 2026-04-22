package com.bikeshop.controller.cliente;

import com.bikeshop.dto.ApiResponse;
import com.bikeshop.dto.mantenimiento.AgendarMantenimientoRequest;
import com.bikeshop.dto.mantenimiento.MantenimientoResponse;
import com.bikeshop.security.UserPrincipal;
import com.bikeshop.service.MantenimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes/mantenimientos")
@RequiredArgsConstructor
public class ClienteMantenimientoController {

    private final MantenimientoService mantenimientoService;

    /** Agendar cita como cliente autenticado */
    @PostMapping
    public ResponseEntity<ApiResponse<MantenimientoResponse>> agendar(
            @Valid @RequestBody AgendarMantenimientoRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        MantenimientoResponse response = mantenimientoService.agendarCliente(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Cita agendada exitosamente.", response));
    }

    /** Ver mis citas */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MantenimientoResponse>>> misMantenimientos(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(mantenimientoService.misMantenimientos(principal.getId())));
    }
}
