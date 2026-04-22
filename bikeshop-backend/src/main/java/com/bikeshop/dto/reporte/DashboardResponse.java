package com.bikeshop.dto.reporte;

import com.bikeshop.dto.mantenimiento.MantenimientoResponse;
import com.bikeshop.dto.pedido.PedidoResponse;
import com.bikeshop.dto.producto.ProductoResumenResponse;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardResponse {
    // Resumen del día
    private int pedidosPendientes;
    private int mantenimientosHoy;
    private int mantenimientosPendientes;
    private int productosStockBajo;
    private BigDecimal ventasHoy;

    // Listas para mostrar en el panel
    private List<MantenimientoResponse> citasHoy;
    private List<PedidoResponse> ultimosPedidos;
    private List<ProductoResumenResponse> alertasStock;
}
