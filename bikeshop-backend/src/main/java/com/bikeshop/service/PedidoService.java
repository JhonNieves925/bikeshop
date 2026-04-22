package com.bikeshop.service;

import com.bikeshop.dto.pedido.*;
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
public class PedidoService {

    private final PedidoRepository pedidoRepo;
    private final PedidoItemRepository pedidoItemRepo;
    private final ProductoRepository productoRepo;
    private final ClienteRepository clienteRepo;
    private final UsuarioRepository usuarioRepo;
    private final InventarioMovimientoRepository inventarioRepo;
    private final NotificacionService notificacionService;
    private final CuponService cuponService;

    // ─── CREAR PEDIDO (invitado) ─────────────────────────────────────────────

    @Transactional
    public PedidoResponse crearPedidoInvitado(CrearPedidoRequest request) {
        return crearPedido(request, null);
    }

    // ─── CREAR PEDIDO (cliente registrado) ──────────────────────────────────

    @Transactional
    public PedidoResponse crearPedidoCliente(CrearPedidoRequest request, Long clienteId) {
        return crearPedido(request, clienteId);
    }

    // ─── LÓGICA COMPARTIDA ──────────────────────────────────────────────────

    private PedidoResponse crearPedido(CrearPedidoRequest request, Long clienteId) {
        // 1. Validar stock y calcular totales
        List<Producto> productos = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (PedidoItemRequest itemReq : request.getItems()) {
            Producto producto = productoRepo.findById(itemReq.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Producto no encontrado: " + itemReq.getProductoId()));

            if (!Boolean.TRUE.equals(producto.getActivo())) {
                throw new IllegalArgumentException("El producto '" + producto.getNombre() + "' no está disponible");
            }
            if (producto.getStock() < itemReq.getCantidad()) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para '" + producto.getNombre() +
                        "'. Disponible: " + producto.getStock());
            }

            subtotal = subtotal.add(producto.getPrecio().multiply(BigDecimal.valueOf(itemReq.getCantidad())));
            productos.add(producto);
        }

        // 2. Aplicar cupón si viene
        BigDecimal descuento = BigDecimal.ZERO;
        BigDecimal total = subtotal;
        String cuponCodigo = request.getCuponCodigo();
        if (cuponCodigo != null && !cuponCodigo.isBlank()) {
            try {
                com.bikeshop.dto.cupon.ValidarCuponRequest vReq = new com.bikeshop.dto.cupon.ValidarCuponRequest();
                vReq.setCodigo(cuponCodigo);
                vReq.setSubtotal(subtotal);
                com.bikeshop.dto.cupon.ValidarCuponResponse vRes = cuponService.validar(vReq);
                descuento = vRes.getMontoDescuento();
                total = vRes.getTotalConDescuento();
            } catch (Exception ignored) {
                // Si el cupón ya no es válido al confirmar, se ignora silenciosamente
            }
        }

        // 3. Construir el pedido
        Cliente cliente = clienteId != null
                ? clienteRepo.findById(clienteId).orElse(null)
                : null;

        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .nombreCliente(request.getNombreCliente())
                .emailCliente(request.getEmailCliente())
                .celularCliente(request.getCelularCliente())
                .direccionEntrega(request.getDireccionEntrega())
                .ciudadEntrega(request.getCiudadEntrega())
                .departamentoEntrega(request.getDepartamentoEntrega())
                .notasEntrega(request.getNotasEntrega())
                .tipoComprador(cliente != null ? Pedido.TipoComprador.REGISTRADO : Pedido.TipoComprador.INVITADO)
                .subtotal(subtotal)
                .descuento(descuento)
                .cuponCodigo(descuento.compareTo(java.math.BigDecimal.ZERO) > 0 ? cuponCodigo : null)
                .total(total)
                .build();

        pedido = pedidoRepo.save(pedido);

        // 3. Crear ítems y descontar stock
        List<PedidoItem> items = new ArrayList<>();
        for (int i = 0; i < request.getItems().size(); i++) {
            PedidoItemRequest itemReq = request.getItems().get(i);
            Producto producto = productos.get(i);

            BigDecimal itemSubtotal = producto.getPrecio().multiply(BigDecimal.valueOf(itemReq.getCantidad()));

            PedidoItem item = PedidoItem.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .cantidad(itemReq.getCantidad())
                    .precioUnitario(producto.getPrecio())
                    .subtotal(itemSubtotal)
                    .build();
            items.add(pedidoItemRepo.save(item));

            // Descontar stock
            int stockAnterior = producto.getStock();
            producto.setStock(stockAnterior - itemReq.getCantidad());
            productoRepo.save(producto);

            // Registrar movimiento de inventario
            InventarioMovimiento movimiento = InventarioMovimiento.builder()
                    .producto(producto)
                    .tipoMovimiento(InventarioMovimiento.TipoMovimiento.VENTA_WEB)
                    .cantidad(-itemReq.getCantidad())
                    .stockAnterior(stockAnterior)
                    .stockNuevo(producto.getStock())
                    .referenciaId(pedido.getId())
                    .motivo("Venta web - Pedido #" + pedido.getId())
                    .build();
            inventarioRepo.save(movimiento);
        }

        // Registrar uso del cupón
        if (cuponCodigo != null && !cuponCodigo.isBlank() && descuento.compareTo(BigDecimal.ZERO) > 0) {
            cuponService.registrarUso(cuponCodigo);
        }

        // Notificar al cliente
        notificacionService.notificarPedidoCreado(
                pedido.getId(),
                pedido.getNombreCliente(),
                pedido.getEmailCliente(),
                pedido.getCelularCliente(),
                pedido.getTotal().toPlainString());

        return PedidoResponse.from(pedido, items);
    }

    // ─── CONSULTAS CLIENTE ──────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<PedidoResponse> misPedidos(Long clienteId) {
        return pedidoRepo.findByClienteIdOrderByCreadoEnDesc(clienteId).stream()
                .map(p -> PedidoResponse.from(p, pedidoItemRepo.findByPedidoId(p.getId())))
                .toList();
    }

    // ─── CONSULTAS ADMIN ────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<PedidoResponse> listarTodos(Pageable pageable) {
        return pedidoRepo.findAllByOrderByCreadoEnDesc(pageable)
                .map(p -> PedidoResponse.from(p, pedidoItemRepo.findByPedidoId(p.getId())));
    }
    @Transactional(readOnly = true)
    public Page<PedidoResponse> listarPorEstado(Pedido.Estado estado, Pageable pageable) {
        return pedidoRepo.findByEstadoOrderByCreadoEnDesc(estado, pageable)
                .map(p -> PedidoResponse.from(p, pedidoItemRepo.findByPedidoId(p.getId())));
    }
    @Transactional(readOnly = true)
    public PedidoResponse detalle(Long id) {
        Pedido p = pedidoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));
        return PedidoResponse.from(p, pedidoItemRepo.findByPedidoId(id));
    }

    @Transactional
    public PedidoResponse actualizarEstado(Long pedidoId, Pedido.Estado nuevoEstado, Long usuarioId) {
        Pedido pedido = pedidoRepo.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));

        Pedido.Estado estadoActual = pedido.getEstado();
        validarTransicionEstado(estadoActual, nuevoEstado);

        pedido.setEstado(nuevoEstado);

        if (nuevoEstado == Pedido.Estado.DESPACHADO) {
            Usuario usuario = usuarioRepo.findById(usuarioId).orElse(null);
            pedido.setDespachadoPor(usuario);
            pedido.setFechaDespacho(LocalDateTime.now());
            notificacionService.notificarPedidoDespachado(
                    pedidoId,
                    pedido.getNombreCliente(),
                    pedido.getEmailCliente(),
                    pedido.getCelularCliente());
        }

        // Si se cancela antes de entregarse, devolver el stock
        if (nuevoEstado == Pedido.Estado.CANCELADO &&
            estadoActual != Pedido.Estado.ENTREGADO) {
            devolverStock(pedidoId);
        }

        pedido = pedidoRepo.save(pedido);
        return PedidoResponse.from(pedido, pedidoItemRepo.findByPedidoId(pedidoId));
    }

    private void validarTransicionEstado(Pedido.Estado actual, Pedido.Estado nuevo) {
        if (actual == Pedido.Estado.CANCELADO || actual == Pedido.Estado.ENTREGADO) {
            throw new IllegalStateException("No se puede cambiar el estado de un pedido " + actual.name());
        }
    }

    private void devolverStock(Long pedidoId) {
        pedidoItemRepo.findByPedidoId(pedidoId).forEach(item -> {
            Producto producto = item.getProducto();
            int stockAnterior = producto.getStock();
            producto.setStock(stockAnterior + item.getCantidad());
            productoRepo.save(producto);

            InventarioMovimiento movimiento = InventarioMovimiento.builder()
                    .producto(producto)
                    .tipoMovimiento(InventarioMovimiento.TipoMovimiento.DEVOLUCION)
                    .cantidad(item.getCantidad())
                    .stockAnterior(stockAnterior)
                    .stockNuevo(producto.getStock())
                    .referenciaId(pedidoId)
                    .motivo("Cancelación pedido #" + pedidoId)
                    .build();
            inventarioRepo.save(movimiento);
        });
    }
}
