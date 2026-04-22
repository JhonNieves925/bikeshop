package com.bikeshop.controller.admin;

import com.bikeshop.dto.ApiResponse;
import com.bikeshop.dto.empleado.*;
import com.bikeshop.security.UserPrincipal;
import com.bikeshop.service.EmpleadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/empleados")
@RequiredArgsConstructor
public class AdminEmpleadoController {

    private final EmpleadoService empleadoService;

    // ─── CRUD ────────────────────────────────────────────────────────────────

    /** Solo ADMIN puede listar empleados (información laboral sensible) */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EmpleadoResponse>>> listar(
            @RequestParam(defaultValue = "false") boolean todos) {
        List<EmpleadoResponse> lista = todos
                ? empleadoService.listarTodos()
                : empleadoService.listar();
        return ResponseEntity.ok(ApiResponse.ok(lista));
    }

    /** Solo ADMIN puede ver el detalle de un empleado */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmpleadoResponse>> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(empleadoService.buscarPorId(id)));
    }

    /** Solo ADMIN puede contratar empleados */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmpleadoResponse>> crear(
            @Valid @RequestBody EmpleadoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Empleado creado", empleadoService.crear(request)));
    }

    /** Solo ADMIN puede editar datos de un empleado */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmpleadoResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EmpleadoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Empleado actualizado", empleadoService.actualizar(id, request)));
    }

    /** Solo ADMIN puede dar de baja a un empleado */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        empleadoService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Empleado eliminado"));
    }

    // ─── JORNADAS ────────────────────────────────────────────────────────────

    /** Empleados y admin pueden registrar entrada (fichaje de llegada) */
    @PostMapping("/{id}/jornada/entrada")
    public ResponseEntity<ApiResponse<JornadaResponse>> registrarEntrada(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Entrada registrada",
                empleadoService.registrarEntrada(id, LocalDate.now())));
    }

    /** Empleados y admin pueden registrar salida (fichaje de salida) */
    @PostMapping("/{id}/jornada/salida")
    public ResponseEntity<ApiResponse<JornadaResponse>> registrarSalida(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Salida registrada",
                empleadoService.registrarSalida(id, LocalDate.now())));
    }

    /** Solo ADMIN puede consultar el historial de jornadas de un empleado */
    @GetMapping("/{id}/jornadas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<JornadaResponse>>> jornadas(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(ApiResponse.ok(empleadoService.jornadasEmpleado(id, desde, hasta)));
    }

    // ─── ACTIVIDADES ─────────────────────────────────────────────────────────

    /** Solo ADMIN puede ver las actividades registradas de un empleado */
    @GetMapping("/{id}/actividades")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ActividadResponse>>> actividades(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(ApiResponse.ok(empleadoService.actividadesEmpleado(id, desde, hasta)));
    }

    // ─── PAGOS ───────────────────────────────────────────────────────────────

    /** Solo ADMIN puede generar liquidación de pago para un empleado */
    @PostMapping("/pagos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagoResponse>> generarPago(
            @Valid @RequestBody GenerarPagoRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Pago generado",
                empleadoService.generarPago(request, principal.getId())));
    }

    /** Solo ADMIN puede marcar un pago como realizado */
    @PatchMapping("/pagos/{pagoId}/pagar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagoResponse>> marcarPagado(@PathVariable Long pagoId) {
        return ResponseEntity.ok(ApiResponse.ok("Pago marcado como pagado",
                empleadoService.marcarPagado(pagoId)));
    }

    /** Solo ADMIN puede ver el historial de pagos de un empleado */
    @GetMapping("/{id}/pagos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> pagos(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(empleadoService.pagosEmpleado(id)));
    }
}
