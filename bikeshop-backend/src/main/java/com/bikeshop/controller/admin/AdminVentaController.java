package com.bikeshop.controller.admin;

import com.bikeshop.dto.ApiResponse;
import com.bikeshop.dto.venta.VentaPageResponse;
import com.bikeshop.service.FacturaPdfService;
import com.bikeshop.service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/admin/ventas")
@RequiredArgsConstructor
public class AdminVentaController {

    private final VentaService ventaService;
    private final FacturaPdfService facturaPdfService;

    /**
     * Lista unificada de ventas (pedidos web + facturas presenciales).
     *
     * GET /api/admin/ventas?fuente=WEB|PRESENCIAL|&desde=&hasta=&nombre=&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<VentaPageResponse>> listar(
            @RequestParam(required = false) String fuente,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String nombre,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        LocalDateTime desdeTs = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaTs = hasta != null ? hasta.atTime(LocalTime.MAX) : null;

        PageRequest pageable = PageRequest.of(page, size, Sort.by("fecha").descending());
        VentaPageResponse resultado = ventaService.listar(fuente, desdeTs, hastaTs, nombre, pageable);
        return ResponseEntity.ok(ApiResponse.ok(resultado));
    }

    /**
     * PDF de un pedido web.
     * GET /api/admin/ventas/web/{id}/pdf
     */
    @GetMapping("/web/{id}/pdf")
    public ResponseEntity<byte[]> pdfWeb(@PathVariable Long id) {
        byte[] contenido = facturaPdfService.generarPdfWeb(id);
        String nombre = "bikeshop-pedido-P" + id + "-" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(nombre).build().toString())
                .body(contenido);
    }

    /**
     * PDF de una factura presencial.
     * GET /api/admin/ventas/presencial/{id}/pdf
     */
    @GetMapping("/presencial/{id}/pdf")
    public ResponseEntity<byte[]> pdfPresencial(@PathVariable Long id) {
        byte[] contenido = facturaPdfService.generarPdfPresencial(id);
        String nombre = "bikeshop-factura-F" + id + "-" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(nombre).build().toString())
                .body(contenido);
    }
}
