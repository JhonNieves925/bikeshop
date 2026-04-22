package com.bikeshop.controller.admin;

import com.bikeshop.dto.ApiResponse;
import com.bikeshop.dto.reporte.ReporteGananciasResponse;
import com.bikeshop.service.ReporteExportService;
import com.bikeshop.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Reportes financieros — acceso exclusivo ADMIN.
 * Los empleados no deben ver ingresos, ganancias ni exportar datos contables.
 */
@RestController
@RequestMapping("/api/admin/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReporteController {

    private final ReporteService reporteService;
    private final ReporteExportService exportService;

    /**
     * Reporte diario.
     * GET /api/admin/reportes/diario?fecha=2026-04-13
     */
    @GetMapping("/diario")
    public ResponseEntity<ApiResponse<ReporteGananciasResponse>> diario(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(reporteService.reporteDiario(dia)));
    }

    /**
     * Reporte semanal — pasa cualquier día de la semana.
     * GET /api/admin/reportes/semanal?fecha=2026-04-13
     */
    @GetMapping("/semanal")
    public ResponseEntity<ApiResponse<ReporteGananciasResponse>> semanal(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(reporteService.reporteSemanal(dia)));
    }

    /**
     * Reporte mensual.
     * GET /api/admin/reportes/mensual?anio=2026&mes=4
     */
    @GetMapping("/mensual")
    public ResponseEntity<ApiResponse<ReporteGananciasResponse>> mensual(
            @RequestParam(defaultValue = "0") int anio,
            @RequestParam(defaultValue = "0") int mes) {
        LocalDate hoy = LocalDate.now();
        int a = anio > 0 ? anio : hoy.getYear();
        int m = mes > 0 ? mes : hoy.getMonthValue();
        return ResponseEntity.ok(ApiResponse.ok(reporteService.reporteMensual(a, m)));
    }

    /**
     * Reporte anual.
     * GET /api/admin/reportes/anual?anio=2026
     */
    @GetMapping("/anual")
    public ResponseEntity<ApiResponse<ReporteGananciasResponse>> anual(
            @RequestParam(defaultValue = "0") int anio) {
        int a = anio > 0 ? anio : LocalDate.now().getYear();
        return ResponseEntity.ok(ApiResponse.ok(reporteService.reporteAnual(a)));
    }

    // ── Exportar Excel ───────────────────────────────────────────────────────

    /**
     * GET /api/admin/reportes/exportar/excel?tipo=semanal|mensual|anual&fecha=&anio=&mes=
     */
    @GetMapping("/exportar/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(defaultValue = "mensual") String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(defaultValue = "0") int anio,
            @RequestParam(defaultValue = "0") int mes) {

        byte[] contenido = exportService.generarExcel(tipo, fecha, anio, mes);
        String nombreArchivo = "bikeshop-reporte-" + tipo + "-" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombreArchivo).build().toString())
                .body(contenido);
    }

    // ── Exportar PDF ─────────────────────────────────────────────────────────

    /**
     * GET /api/admin/reportes/exportar/pdf?tipo=semanal|mensual|anual&fecha=&anio=&mes=
     */
    @GetMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(defaultValue = "mensual") String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(defaultValue = "0") int anio,
            @RequestParam(defaultValue = "0") int mes) {

        byte[] contenido = exportService.generarPdf(tipo, fecha, anio, mes);
        String nombreArchivo = "bikeshop-reporte-" + tipo + "-" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombreArchivo).build().toString())
                .body(contenido);
    }
}
