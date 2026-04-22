package com.bikeshop.controller.pub;

import com.bikeshop.dto.ApiResponse;
import com.bikeshop.dto.mantenimiento.AgendarMantenimientoRequest;
import com.bikeshop.dto.mantenimiento.DisponibilidadResponse;
import com.bikeshop.dto.mantenimiento.MantenimientoResponse;
import com.bikeshop.service.MantenimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/public/mantenimientos")
@RequiredArgsConstructor
public class PublicMantenimientoController {

    private final MantenimientoService mantenimientoService;

    /**
     * Consulta disponibilidad de un día (para mostrar slots en el calendario).
     * Ej: GET /api/public/mantenimientos/disponibilidad?fecha=2026-04-20
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<ApiResponse<DisponibilidadResponse>> disponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(ApiResponse.ok(mantenimientoService.consultarDisponibilidad(fecha)));
    }

    /**
     * Disponibilidad de un rango de fechas (para renderizar el mes completo).
     * Ej: GET /api/public/mantenimientos/disponibilidad/rango?desde=2026-04-01&hasta=2026-04-30
     */
    @GetMapping("/disponibilidad/rango")
    public ResponseEntity<ApiResponse<List<DisponibilidadResponse>>> disponibilidadRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(ApiResponse.ok(mantenimientoService.consultarDisponibilidadRango(desde, hasta)));
    }

    /**
     * Agendar cita sin cuenta (invitado).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MantenimientoResponse>> agendarInvitado(
            @Valid @RequestBody AgendarMantenimientoRequest request) {
        MantenimientoResponse response = mantenimientoService.agendarInvitado(request);
        return ResponseEntity.ok(ApiResponse.ok("Cita agendada exitosamente. Te esperamos.", response));
    }
}
