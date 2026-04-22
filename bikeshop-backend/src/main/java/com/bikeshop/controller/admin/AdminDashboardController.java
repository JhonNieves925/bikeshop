package com.bikeshop.controller.admin;

import com.bikeshop.dto.ApiResponse;
import com.bikeshop.dto.reporte.DashboardResponse;
import com.bikeshop.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    /**
     * Resumen del día para la pantalla principal del panel admin.
     * Incluye: pedidos pendientes, citas de hoy, alertas de stock, ventas del día.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.obtener()));
    }
}
