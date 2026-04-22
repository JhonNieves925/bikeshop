package com.bikeshop.service;

import com.bikeshop.dto.venta.VentaDetalleResponse;
import com.bikeshop.dto.venta.VentaPageResponse;
import com.bikeshop.entity.Factura;
import com.bikeshop.entity.Pedido;
import com.bikeshop.repository.FacturaItemRepository;
import com.bikeshop.repository.FacturaRepository;
import com.bikeshop.repository.PedidoItemRepository;
import com.bikeshop.repository.PedidoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final PedidoRepository pedidoRepo;
    private final PedidoItemRepository pedidoItemRepo;
    private final FacturaRepository facturaRepo;
    private final FacturaItemRepository facturaItemRepo;

    /**
     * Lista unificada de ventas (pedidos web + facturas presenciales).
     *
     * @param fuente "WEB" | "PRESENCIAL" | null/vacío = todos
     * @param desde  filtro de fecha inicio (puede ser null)
     * @param hasta  filtro de fecha fin (puede ser null)
     * @param nombre filtro por nombre de cliente (contiene, ignorando mayúsculas)
     */
    @Transactional(readOnly = true)
    public VentaPageResponse listar(String fuente,
                                     LocalDateTime desde,
                                     LocalDateTime hasta,
                                     String nombre,
                                     Pageable pageable) {

        List<VentaDetalleResponse> todos = new ArrayList<>();
        boolean incluirWeb        = fuente == null || fuente.isBlank() || "WEB".equalsIgnoreCase(fuente);
        boolean incluirPresencial = fuente == null || fuente.isBlank() || "PRESENCIAL".equalsIgnoreCase(fuente);

        // ── Pedidos web ──────────────────────────────────────────────────────
        if (incluirWeb) {
            List<Pedido> pedidos;
            if (desde != null && hasta != null) {
                pedidos = pedidoRepo.findByRangoFechas(desde, hasta);
            } else if (desde != null) {
                pedidos = pedidoRepo.findByRangoFechas(desde, LocalDateTime.now());
            } else {
                pedidos = pedidoRepo.findAllByOrderByCreadoEnDesc(
                        org.springframework.data.domain.Pageable.unpaged()).getContent();
            }

            for (Pedido p : pedidos) {
                if (nombre != null && !nombre.isBlank()) {
                    if (p.getNombreCliente() == null ||
                        !p.getNombreCliente().toLowerCase().contains(nombre.toLowerCase())) {
                        continue;
                    }
                }
                todos.add(VentaDetalleResponse.from(p, pedidoItemRepo.findByPedidoId(p.getId())));
            }
        }

        // ── Facturas presenciales ────────────────────────────────────────────
        if (incluirPresencial) {
            List<Factura> facturas;
            if (desde != null && hasta != null) {
                facturas = facturaRepo.findByFechaEmisionBetween(desde, hasta);
            } else if (desde != null) {
                facturas = facturaRepo.findByFechaEmisionBetween(desde, LocalDateTime.now());
            } else {
                facturas = facturaRepo.findAll();
            }

            for (Factura f : facturas) {
                if (nombre != null && !nombre.isBlank()) {
                    if (f.getNombreCliente() == null ||
                        !f.getNombreCliente().toLowerCase().contains(nombre.toLowerCase())) {
                        continue;
                    }
                }
                todos.add(VentaDetalleResponse.from(f, facturaItemRepo.findByFacturaId(f.getId())));
            }
        }

        // Ordenar por fecha descendente
        todos.sort(Comparator.comparing(VentaDetalleResponse::getFecha,
                Comparator.nullsLast(Comparator.reverseOrder())));

        // Suma monetaria total de TODOS los resultados del filtro
        BigDecimal totalImporte = todos.stream()
                .map(v -> v.getTotal() != null ? v.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Paginación manual
        int total = todos.size();
        int offset = (int) pageable.getOffset();
        int toIndex = Math.min(offset + pageable.getPageSize(), total);
        List<VentaDetalleResponse> pagina = offset >= total
                ? List.of()
                : todos.subList(offset, toIndex);

        int numPages = pageable.getPageSize() > 0
                ? (int) Math.ceil((double) total / pageable.getPageSize())
                : 1;

        return VentaPageResponse.builder()
                .content(pagina)
                .totalPages(numPages)
                .totalElements(total)
                .totalImporte(totalImporte)
                .build();
    }

    /** Detalle completo de una venta web (pedido). */
    @Transactional(readOnly = true)
    public VentaDetalleResponse detalleWeb(Long pedidoId) {
        Pedido p = pedidoRepo.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado: " + pedidoId));
        return VentaDetalleResponse.from(p, pedidoItemRepo.findByPedidoId(pedidoId));
    }

    /** Detalle completo de una venta presencial (factura). */
    @Transactional(readOnly = true)
    public VentaDetalleResponse detallePresencial(Long facturaId) {
        Factura f = facturaRepo.findById(facturaId)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada: " + facturaId));
        return VentaDetalleResponse.from(f, facturaItemRepo.findByFacturaId(facturaId));
    }
}
