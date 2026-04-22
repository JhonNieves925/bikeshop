package com.bikeshop.dto.reporte;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ReporteGananciasResponse {
    private String periodo;           // "2026-04-13", "2026-W15", "2026-04", "2026"
    private BigDecimal totalGeneral;
    private BigDecimal totalWeb;
    private BigDecimal totalPresencial;
    private BigDecimal totalMantenimiento;
    private int cantidadVentasWeb;
    private int cantidadVentasPresencial;
    private int cantidadMantenimientos;
    private List<PuntoGrafica> serieWeb;
    private List<PuntoGrafica> seriePresencial;
    private List<PuntoGrafica> serieMantenimiento;

    @Data
    @Builder
    public static class PuntoGrafica {
        private String etiqueta;   // "Lun", "Mar" / "Sem 1" / "Ene" / "2024"
        private BigDecimal valor;
    }
}
