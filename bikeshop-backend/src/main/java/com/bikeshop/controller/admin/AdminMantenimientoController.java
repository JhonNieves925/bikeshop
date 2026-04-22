package com.bikeshop.controller.admin;

import com.bikeshop.dto.ApiResponse;
import com.bikeshop.dto.mantenimiento.AgendarMantenimientoRequest;
import com.bikeshop.dto.mantenimiento.DisponibilidadResponse;
import com.bikeshop.dto.mantenimiento.GestionarMantenimientoRequest;
import com.bikeshop.dto.mantenimiento.MantenimientoResponse;
import com.bikeshop.entity.Mantenimiento;
import com.bikeshop.security.UserPrincipal;
import com.bikeshop.service.MantenimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/mantenimientos")
@RequiredArgsConstructor
public class AdminMantenimientoController {

    private final MantenimientoService mantenimientoService;

    /**
     * Vista del día — lo que usa el mecánico cada mañana.
     * GET /api/admin/mantenimientos/dia?fecha=2026-04-20
     * Si no se pasa fecha, usa hoy.
     */
    @GetMapping("/dia")
    public ResponseEntity<ApiResponse<List<MantenimientoResponse>>> porDia(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(mantenimientoService.listarPorFecha(dia)));
    }

    /**
     * Vista semanal / rango de fechas — para el calendario global.
     * GET /api/admin/mantenimientos/rango?desde=2026-04-14&hasta=2026-04-20
     */
    @GetMapping("/rango")
    public ResponseEntity<ApiResponse<List<MantenimientoResponse>>> porRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(ApiResponse.ok(mantenimientoService.listarPorRango(desde, hasta)));
    }

    /**
     * Listar por estado con paginación.
     * GET /api/admin/mantenimientos?estado=PENDIENTE
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MantenimientoResponse>>> porEstado(
            @RequestParam(defaultValue = "PENDIENTE") Mantenimiento.Estado estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                mantenimientoService.listarPorEstado(estado, PageRequest.of(page, size))));
    }

    /** Detalle completo de un mantenimiento */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MantenimientoResponse>> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(mantenimientoService.detalle(id)));
    }

    /**
     * Gestionar mantenimiento: cambiar estado, agregar diagnóstico, repuestos, costos.
     * PATCH /api/admin/mantenimientos/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<MantenimientoResponse>> gestionar(
            @PathVariable Long id,
            @Valid @RequestBody GestionarMantenimientoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Mantenimiento actualizado",
                mantenimientoService.gestionar(id, request)));
    }

    /**
     * Crear cita presencial desde el panel admin/empleado.
     * Reutiliza la misma lógica que el endpoint público (sin vincular cuenta de cliente).
     * POST /api/admin/mantenimientos
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MantenimientoResponse>> crearCitaPresencial(
            @Valid @RequestBody AgendarMantenimientoRequest request) {
        MantenimientoResponse response = mantenimientoService.agendarInvitado(request);
        return ResponseEntity.ok(ApiResponse.ok("Cita agendada exitosamente", response));
    }

    // ─── DÍAS BLOQUEADOS ────────────────────────────────────────────────────

    /** Solo ADMIN puede bloquear días (festivos, vacaciones, cierre especial) */
    @PostMapping("/dias-bloqueados")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> bloquearDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) String motivo,
            @AuthenticationPrincipal UserPrincipal principal) {
        mantenimientoService.bloquearDia(fecha, motivo, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Día bloqueado"));
    }

    /** Solo ADMIN puede desbloquear días */
    @DeleteMapping("/dias-bloqueados")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> desbloquearDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        mantenimientoService.desbloquearDia(fecha);
        return ResponseEntity.ok(ApiResponse.ok("Día desbloqueado"));
    }

    /** Ver disponibilidad desde el panel (misma lógica que el público) */
    @GetMapping("/disponibilidad")
    public ResponseEntity<ApiResponse<DisponibilidadResponse>> disponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(ApiResponse.ok(mantenimientoService.consultarDisponibilidad(fecha)));
    }
}
