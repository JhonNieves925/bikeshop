package com.bikeshop.dto.mantenimiento;

import com.bikeshop.entity.Mantenimiento;
import com.bikeshop.entity.MantenimientoRepuesto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class MantenimientoResponse {
    private Long id;
    private String nombreCliente;
    private String emailCliente;
    private String celularCliente;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFinReal;
    private String estado;
    private String tipoBicicleta;
    private String marcaBicicleta;
    private String modeloBicicleta;
    private String problemaReportado;
    private String diagnostico;
    private String trabajosRealizados;
    private String notasInternas;
    private BigDecimal costoManoObra;
    private BigDecimal costoRepuestos;
    private BigDecimal costoTotal;
    private String atendidoPorNombre;
    private Long bicicletaId;
    private List<RepuestoUsadoResponse> repuestos;
    private LocalDateTime creadoEn;

    @Data
    @Builder
    public static class RepuestoUsadoResponse {
        private Long productoId;
        private String productoNombre;
        private int cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;

        public static RepuestoUsadoResponse from(MantenimientoRepuesto r) {
            boolean tieneProducto = r.getProducto() != null;
            return RepuestoUsadoResponse.builder()
                    .productoId(tieneProducto ? r.getProducto().getId() : null)
                    .productoNombre(tieneProducto ? r.getProducto().getNombre() : "Producto eliminado")
                    .cantidad(r.getCantidad())
                    .precioUnitario(r.getPrecioUnitario())
                    .subtotal(r.getSubtotal())
                    .build();
        }
    }

    public static MantenimientoResponse from(Mantenimiento m) {
        return MantenimientoResponse.builder()
                .id(m.getId())
                .nombreCliente(m.getNombreCliente())
                .emailCliente(m.getEmailCliente())
                .celularCliente(m.getCelularCliente())
                .fecha(m.getFecha())
                .horaInicio(m.getHoraInicio())
                .horaFinReal(m.getHoraFinReal())
                .estado(m.getEstado().name())
                .tipoBicicleta(m.getTipoBicicleta())
                .marcaBicicleta(m.getMarcaBicicleta())
                .modeloBicicleta(m.getModeloBicicleta())
                .problemaReportado(m.getProblemaReportado())
                .diagnostico(m.getDiagnostico())
                .trabajosRealizados(m.getTrabajosRealizados())
                .notasInternas(m.getNotasInternas())
                .costoManoObra(m.getCostoManoObra())
                .costoRepuestos(m.getCostoRepuestos())
                .costoTotal(m.getCostoTotal())
                .atendidoPorNombre(m.getAtendidoPor() != null ? m.getAtendidoPor().getNombre() : null)
                .bicicletaId(m.getBicicleta() != null ? m.getBicicleta().getId() : null)
                .repuestos(m.getRepuestos() != null
                        ? m.getRepuestos().stream().map(RepuestoUsadoResponse::from).toList()
                        : List.of())
                .creadoEn(m.getCreadoEn())
                .build();
    }
}
