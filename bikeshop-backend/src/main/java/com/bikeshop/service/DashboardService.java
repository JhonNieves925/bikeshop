package com.bikeshop.service;

import com.bikeshop.dto.mantenimiento.MantenimientoResponse;
import com.bikeshop.dto.pedido.PedidoResponse;
import com.bikeshop.dto.producto.ProductoResumenResponse;
import com.bikeshop.dto.reporte.DashboardResponse;
import com.bikeshop.entity.Factura;
import com.bikeshop.entity.Mantenimiento;
import com.bikeshop.entity.Pedido;
import com.bikeshop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PedidoRepository pedidoRepo;
    private final PedidoItemRepository pedidoItemRepo;
    private final MantenimientoRepository mantenimientoRepo;
    private final ProductoRepository productoRepo;
    private final ProductoImagenRepository productoImagenRepo;
    private final FacturaRepository facturaRepo;

    public DashboardResponse obtener() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime finHoy = hoy.atTime(LocalTime.MAX);

        // Citas de hoy
        List<MantenimientoResponse> citasHoy = mantenimientoRepo
                .findByFechaOrderByHoraInicioAsc(hoy)
                .stream().map(MantenimientoResponse::from).toList();

        // Contadores
        int pedidosPendientes = (int) pedidoRepo
                .findByEstadoOrderByCreadoEnDesc(Pedido.Estado.PENDIENTE, PageRequest.of(0, Integer.MAX_VALUE))
                .getTotalElements();

        int mantenimientosPendientes = (int) mantenimientoRepo
                .findByEstadoOrderByFechaAsc(Mantenimiento.Estado.PENDIENTE, PageRequest.of(0, Integer.MAX_VALUE))
                .getTotalElements();

        // Productos con stock bajo
        List<ProductoResumenResponse> alertasStock = productoRepo.findProductosConStockBajo()
                .stream().map(p -> ProductoResumenResponse.from(p,
                        productoImagenRepo.findByProductoIdOrderByOrdenAsc(p.getId()),
                        List.of()))
                .toList();

        // Ventas de hoy (facturas emitidas hoy)
        BigDecimal ventasHoy = facturaRepo.findByFechaEmisionBetween(inicioHoy, finHoy)
                .stream().map(Factura::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Últimos 5 pedidos
        List<PedidoResponse> ultimosPedidos = pedidoRepo
                .findAllByOrderByCreadoEnDesc(PageRequest.of(0, 5))
                .stream()
                .map(p -> PedidoResponse.from(p, pedidoItemRepo.findByPedidoId(p.getId())))
                .toList();

        return DashboardResponse.builder()
                .pedidosPendientes(pedidosPendientes)
                .mantenimientosHoy(citasHoy.size())
                .mantenimientosPendientes(mantenimientosPendientes)
                .productosStockBajo(alertasStock.size())
                .ventasHoy(ventasHoy)
                .citasHoy(citasHoy)
                .ultimosPedidos(ultimosPedidos)
                .alertasStock(alertasStock)
                .build();
    }
}
