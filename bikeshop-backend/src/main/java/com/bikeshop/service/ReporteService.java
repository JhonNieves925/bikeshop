package com.bikeshop.service;

import com.bikeshop.dto.reporte.ReporteGananciasResponse;
import com.bikeshop.dto.reporte.ReporteGananciasResponse.PuntoGrafica;
import com.bikeshop.entity.Factura;
import com.bikeshop.entity.Mantenimiento;
import com.bikeshop.entity.Pedido;
import com.bikeshop.repository.FacturaRepository;
import com.bikeshop.repository.MantenimientoRepository;
import com.bikeshop.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final PedidoRepository pedidoRepo;
    private final MantenimientoRepository mantenimientoRepo;
    private final FacturaRepository facturaRepo;

    // ─── REPORTE DIARIO ──────────────────────────────────────────────────────

    public ReporteGananciasResponse reporteDiario(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);

        List<Pedido> pedidos = pedidoRepo.findByRangoFechas(inicio, fin).stream()
                .filter(p -> p.getEstado() != Pedido.Estado.CANCELADO)
                .collect(Collectors.toList());

        List<Factura> facturas = facturaRepo.findByFechaEmisionBetween(inicio, fin);

        List<Mantenimiento> mantenimientos = mantenimientoRepo
                .findByFechaOrderByHoraInicioAsc(fecha).stream()
                .filter(m -> m.getEstado() == Mantenimiento.Estado.FINALIZADO)
                .collect(Collectors.toList());

        return construirReporte(pedidos, facturas, mantenimientos,
                fecha.toString(), List.of(), List.of());
    }

    // ─── REPORTE SEMANAL ─────────────────────────────────────────────────────

    public ReporteGananciasResponse reporteSemanal(LocalDate cualquierDia) {
        LocalDate lunes = cualquierDia.with(DayOfWeek.MONDAY);
        LocalDate domingo = cualquierDia.with(DayOfWeek.SUNDAY);
        LocalDateTime inicio = lunes.atStartOfDay();
        LocalDateTime fin = domingo.atTime(LocalTime.MAX);

        List<Pedido> pedidos = pedidoRepo.findByRangoFechas(inicio, fin).stream()
                .filter(p -> p.getEstado() != Pedido.Estado.CANCELADO)
                .collect(Collectors.toList());

        List<Factura> facturas = facturaRepo.findByFechaEmisionBetween(inicio, fin);

        List<Mantenimiento> mantenimientos = mantenimientoRepo
                .findByFechaBetweenOrderByFechaAscHoraInicioAsc(lunes, domingo).stream()
                .filter(m -> m.getEstado() == Mantenimiento.Estado.FINALIZADO)
                .collect(Collectors.toList());

        // Serie por día de la semana (Lun – Dom)
        List<PuntoGrafica> serieWeb = new ArrayList<>();
        List<PuntoGrafica> seriePresencial = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate dia = lunes.plusDays(i);
            LocalDateTime diaInicio = dia.atStartOfDay();
            LocalDateTime diaFin = dia.atTime(LocalTime.MAX);
            String etiqueta = dia.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("es", "CO"));

            BigDecimal totalWebDia = pedidos.stream()
                    .filter(p -> !p.getCreadoEn().isBefore(diaInicio) && !p.getCreadoEn().isAfter(diaFin))
                    .map(Pedido::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalPresencialDia = facturas.stream()
                    .filter(f -> !f.getFechaEmision().isBefore(diaInicio) && !f.getFechaEmision().isAfter(diaFin))
                    .map(Factura::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            serieWeb.add(PuntoGrafica.builder().etiqueta(etiqueta).valor(totalWebDia).build());
            seriePresencial.add(PuntoGrafica.builder().etiqueta(etiqueta).valor(totalPresencialDia).build());
        }

        String periodo = "Semana " + lunes + " / " + domingo;
        return construirReporte(pedidos, facturas, mantenimientos, periodo, serieWeb, seriePresencial);
    }

    // ─── REPORTE MENSUAL ─────────────────────────────────────────────────────

    public ReporteGananciasResponse reporteMensual(int anio, int mes) {
        LocalDate primero = LocalDate.of(anio, mes, 1);
        LocalDate ultimo = primero.withDayOfMonth(primero.lengthOfMonth());
        LocalDateTime inicio = primero.atStartOfDay();
        LocalDateTime fin = ultimo.atTime(LocalTime.MAX);

        List<Pedido> pedidos = pedidoRepo.findByRangoFechas(inicio, fin).stream()
                .filter(p -> p.getEstado() != Pedido.Estado.CANCELADO)
                .collect(Collectors.toList());

        List<Factura> facturas = facturaRepo.findByFechaEmisionBetween(inicio, fin);

        List<Mantenimiento> mantenimientos = mantenimientoRepo
                .findByFechaBetweenOrderByFechaAscHoraInicioAsc(primero, ultimo).stream()
                .filter(m -> m.getEstado() == Mantenimiento.Estado.FINALIZADO)
                .collect(Collectors.toList());

        // Serie por semana del mes
        List<PuntoGrafica> serieWeb = new ArrayList<>();
        List<PuntoGrafica> seriePresencial = new ArrayList<>();
        LocalDate cursor = primero;
        int numSemana = 1;
        while (!cursor.isAfter(ultimo)) {
            LocalDate finSemana = cursor.plusDays(6).isAfter(ultimo) ? ultimo : cursor.plusDays(6);
            LocalDateTime semInicio = cursor.atStartOfDay();
            LocalDateTime semFin = finSemana.atTime(LocalTime.MAX);
            String etiqueta = "Sem " + numSemana;

            BigDecimal totalWebSem = pedidos.stream()
                    .filter(p -> !p.getCreadoEn().isBefore(semInicio) && !p.getCreadoEn().isAfter(semFin))
                    .map(Pedido::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalPresencialSem = facturas.stream()
                    .filter(f -> !f.getFechaEmision().isBefore(semInicio) && !f.getFechaEmision().isAfter(semFin))
                    .map(Factura::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            serieWeb.add(PuntoGrafica.builder().etiqueta(etiqueta).valor(totalWebSem).build());
            seriePresencial.add(PuntoGrafica.builder().etiqueta(etiqueta).valor(totalPresencialSem).build());
            cursor = cursor.plusDays(7);
            numSemana++;
        }

        String periodo = primero.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "CO")) + " " + anio;
        return construirReporte(pedidos, facturas, mantenimientos, periodo, serieWeb, seriePresencial);
    }

    // ─── REPORTE ANUAL ───────────────────────────────────────────────────────

    public ReporteGananciasResponse reporteAnual(int anio) {
        LocalDateTime inicio = LocalDate.of(anio, 1, 1).atStartOfDay();
        LocalDateTime fin = LocalDate.of(anio, 12, 31).atTime(LocalTime.MAX);

        List<Pedido> pedidos = pedidoRepo.findByRangoFechas(inicio, fin).stream()
                .filter(p -> p.getEstado() != Pedido.Estado.CANCELADO)
                .collect(Collectors.toList());

        List<Factura> facturas = facturaRepo.findByFechaEmisionBetween(inicio, fin);

        List<Mantenimiento> mantenimientos = mantenimientoRepo
                .findByFechaBetweenOrderByFechaAscHoraInicioAsc(
                        LocalDate.of(anio, 1, 1), LocalDate.of(anio, 12, 31)).stream()
                .filter(m -> m.getEstado() == Mantenimiento.Estado.FINALIZADO)
                .collect(Collectors.toList());

        // Serie por mes (Ene – Dic)
        List<PuntoGrafica> serieWeb = new ArrayList<>();
        List<PuntoGrafica> seriePresencial = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            LocalDateTime mesInicio = LocalDate.of(anio, m, 1).atStartOfDay();
            LocalDate ultimoMes = LocalDate.of(anio, m, 1)
                    .withDayOfMonth(LocalDate.of(anio, m, 1).lengthOfMonth());
            LocalDateTime mesFin = ultimoMes.atTime(LocalTime.MAX);
            String etiqueta = Month.of(m).getDisplayName(TextStyle.SHORT, new Locale("es", "CO"));

            BigDecimal totalWebMes = pedidos.stream()
                    .filter(p -> !p.getCreadoEn().isBefore(mesInicio) && !p.getCreadoEn().isAfter(mesFin))
                    .map(Pedido::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalPresencialMes = facturas.stream()
                    .filter(f -> !f.getFechaEmision().isBefore(mesInicio) && !f.getFechaEmision().isAfter(mesFin))
                    .map(Factura::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            serieWeb.add(PuntoGrafica.builder().etiqueta(etiqueta).valor(totalWebMes).build());
            seriePresencial.add(PuntoGrafica.builder().etiqueta(etiqueta).valor(totalPresencialMes).build());
        }

        return construirReporte(pedidos, facturas, mantenimientos,
                String.valueOf(anio), serieWeb, seriePresencial);
    }

    // ─── CONSTRUCCIÓN DEL REPORTE ────────────────────────────────────────────

    private ReporteGananciasResponse construirReporte(List<Pedido> pedidos,
                                                       List<Factura> facturas,
                                                       List<Mantenimiento> mantenimientos,
                                                       String periodo,
                                                       List<PuntoGrafica> serieWeb,
                                                       List<PuntoGrafica> seriePresencial) {
        BigDecimal totalWeb = pedidos.stream()
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPresencial = facturas.stream()
                .map(Factura::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMant = mantenimientos.stream()
                .map(m -> m.getCostoTotal() != null ? m.getCostoTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGeneral = totalWeb.add(totalPresencial).add(totalMant);

        return ReporteGananciasResponse.builder()
                .periodo(periodo)
                .totalGeneral(totalGeneral)
                .totalWeb(totalWeb)
                .totalPresencial(totalPresencial)
                .totalMantenimiento(totalMant)
                .cantidadVentasWeb(pedidos.size())
                .cantidadVentasPresencial(facturas.size())
                .cantidadMantenimientos(mantenimientos.size())
                .serieWeb(serieWeb)
                .seriePresencial(seriePresencial)
                .serieMantenimiento(List.of())
                .build();
    }
}
