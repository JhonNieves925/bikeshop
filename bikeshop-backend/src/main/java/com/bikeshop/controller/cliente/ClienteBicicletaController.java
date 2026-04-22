package com.bikeshop.controller.cliente;

import com.bikeshop.dto.bicicleta.BicicletaRequest;
import com.bikeshop.dto.bicicleta.BicicletaResponse;
import com.bikeshop.dto.mantenimiento.MantenimientoResponse;
import com.bikeshop.security.UserPrincipal;
import com.bikeshop.service.BicicletaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cliente/bicicletas")
@RequiredArgsConstructor
public class ClienteBicicletaController {

    private final BicicletaService bicicletaService;

    @GetMapping
    public List<BicicletaResponse> listar(@AuthenticationPrincipal UserPrincipal user) {
        return bicicletaService.listarPorCliente(user.getId());
    }

    @PostMapping
    public BicicletaResponse crear(@AuthenticationPrincipal UserPrincipal user,
                                   @RequestBody BicicletaRequest req) {
        return bicicletaService.crear(user.getId(), req);
    }

    @PutMapping("/{id}")
    public BicicletaResponse actualizar(@AuthenticationPrincipal UserPrincipal user,
                                        @PathVariable Long id,
                                        @RequestBody BicicletaRequest req) {
        return bicicletaService.actualizar(user.getId(), id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@AuthenticationPrincipal UserPrincipal user,
                                         @PathVariable Long id) {
        bicicletaService.eliminar(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historial")
    public List<MantenimientoResponse> historial(@AuthenticationPrincipal UserPrincipal user,
                                                  @PathVariable Long id) {
        return bicicletaService.historialPorBicicleta(user.getId(), id);
    }
}
