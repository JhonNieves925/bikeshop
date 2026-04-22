package com.bikeshop.service;

import com.bikeshop.dto.factura.*;
import com.bikeshop.entity.*;
import com.bikeshop.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacturaService {

    private final FacturaRepository facturaRepo;
    private final FacturaItemRepository facturaItemRepo;
    private final ProductoRepository productoRepo;
    private final MantenimientoRepository mantenimientoRepo;
    private final UsuarioRepository usuarioRepo;
    private final InventarioMovimientoRepository inventarioRepo;

    // ─── CREAR FACTURA PRESENCIAL ────────────────────────────────────────────

    @Transactional
    public FacturaResponse crear(CrearFacturaRequest request, Long usuarioId) {
        Usuario emitidaPor = usuarioRepo.findById(usuarioId).orElse(null);

        Mantenimiento mantenimiento = null;
        if (request.getMantenimientoId() != null) {
            mantenimiento = mantenimientoRepo.findById(request.getMantenimientoId())
                    .orElseThrow(() -> new EntityNotFoundException("Mantenimiento no encontrado"));
        }

        // Calcular subtotal
        BigDecimal subtotal = request.getItems().stream()
                .map(i -> i.getPrecioUnitario().multiply(BigDecimal.valueOf(i.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal descuento = request.getDescuento() != null ? request.getDescuento() : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(descuento);

        // Determinar tipo
        boolean tieneProductos = request.getItems().stream().anyMatch(i -> i.getProductoId() != null);
        boolean tieneServicios = request.getItems().stream().anyMatch(i -> i.getProductoId() == null);
        Factura.Tipo tipo = (tieneProductos && tieneServicios) ? Factura.Tipo.MIXTA
                : tieneProductos ? Factura.Tipo.VENTA
                : Factura.Tipo.SERVICIO;

        Factura factura = Factura.builder()
                .emitidaPor(emitidaPor)
                .mantenimiento(mantenimiento)
                .nombreCliente(request.getNombreCliente())
                .emailCliente(request.getEmailCliente())
                .canal(request.getCanal())
                .tipo(tipo)
                .subtotal(subtotal)
                .descuento(descuento)
                .total(total)
                .observaciones(request.getObservaciones())
                .build();

        factura = facturaRepo.save(factura);

        // Crear ítems y descontar stock si es producto de inventario
        List<FacturaItem> items = new ArrayList<>();
        for (FacturaItemRequest itemReq : request.getItems()) {
            Producto producto = null;
            if (itemReq.getProductoId() != null) {
                producto = productoRepo.findById(itemReq.getProductoId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Producto no encontrado: " + itemReq.getProductoId()));

                if (producto.getStock() < itemReq.getCantidad()) {
                    throw new IllegalArgumentException(
                            "Stock insuficiente para '" + producto.getNombre() + "'");
                }

                // Descontar stock
                int stockAnterior = producto.getStock();
                producto.setStock(stockAnterior - itemReq.getCantidad());
                productoRepo.save(producto);

                inventarioRepo.save(InventarioMovimiento.builder()
                        .producto(producto)
                        .usuario(emitidaPor)
                        .tipoMovimiento(InventarioMovimiento.TipoMovimiento.VENTA_PRESENCIAL)
                        .cantidad(-itemReq.getCantidad())
                        .stockAnterior(stockAnterior)
                        .stockNuevo(producto.getStock())
                        .referenciaId(factura.getId())
                        .motivo("Venta presencial - Factura #" + factura.getId())
                        .build());
            }

            BigDecimal itemSubtotal = itemReq.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(itemReq.getCantidad()));

            FacturaItem item = FacturaItem.builder()
                    .factura(factura)
                    .producto(producto)
                    .descripcion(itemReq.getDescripcion())
                    .cantidad(itemReq.getCantidad())
                    .precioUnitario(itemReq.getPrecioUnitario())
                    .subtotal(itemSubtotal)
                    .build();

            items.add(facturaItemRepo.save(item));
        }

        return FacturaResponse.from(factura, items);
    }

    // ─── CONSULTAS ──────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<FacturaResponse> listar(LocalDateTime desde, LocalDateTime hasta, String nombre, Pageable pageable) {
        return facturaRepo.buscar(desde, hasta, nombre, pageable)
                .map(f -> FacturaResponse.from(f, facturaItemRepo.findByFacturaId(f.getId())));
    }
    @Transactional(readOnly = true)
    public FacturaResponse detalle(Long id) {
        Factura f = facturaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));
        return FacturaResponse.from(f, facturaItemRepo.findByFacturaId(id));
    }
}
