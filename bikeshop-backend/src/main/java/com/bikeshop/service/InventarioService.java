package com.bikeshop.service;

import com.bikeshop.dto.inventario.MovimientoRequest;
import com.bikeshop.dto.inventario.MovimientoResponse;
import com.bikeshop.entity.InventarioMovimiento;
import com.bikeshop.entity.Producto;
import com.bikeshop.entity.Usuario;
import com.bikeshop.repository.InventarioMovimientoRepository;
import com.bikeshop.repository.ProductoRepository;
import com.bikeshop.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioMovimientoRepository inventarioRepo;
    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;

    /**
     * Registra un movimiento manual: entrada de mercancía, ajuste, devolución.
     * - ENTRADA: suma stock
     * - AJUSTE_MANUAL: puede ser positivo (aumentar) o negativo (corregir)
     * - DEVOLUCION: suma stock
     */
    @Transactional
    public MovimientoResponse registrarMovimiento(MovimientoRequest request, Long usuarioId) {
        Producto producto = productoRepo.findById(request.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        Usuario usuario = usuarioRepo.findById(usuarioId).orElse(null);

        int stockAnterior = producto.getStock();
        int stockNuevo;

        switch (request.getTipoMovimiento()) {
            case ENTRADA, DEVOLUCION -> stockNuevo = stockAnterior + request.getCantidad();
            case AJUSTE_MANUAL -> {
                // Para ajuste, la cantidad puede ser positiva o negativa
                // El request siempre viene positivo, pero el motivo indica si es reducción
                // Usamos un campo negativo si se quiere reducir — el cliente envía cantidad como int
                stockNuevo = stockAnterior + request.getCantidad();
                if (stockNuevo < 0) throw new IllegalArgumentException("El stock no puede quedar negativo");
            }
            default -> throw new IllegalArgumentException(
                    "Solo se permiten movimientos manuales: ENTRADA, AJUSTE_MANUAL, DEVOLUCION");
        }

        producto.setStock(stockNuevo);
        productoRepo.save(producto);

        InventarioMovimiento mov = InventarioMovimiento.builder()
                .producto(producto)
                .usuario(usuario)
                .tipoMovimiento(request.getTipoMovimiento())
                .cantidad(request.getCantidad())
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .motivo(request.getMotivo())
                .build();

        return MovimientoResponse.from(inventarioRepo.save(mov));
    }
    @Transactional(readOnly = true)
    public Page<MovimientoResponse> listarTodos(Pageable pageable) {
        return inventarioRepo.findAllByOrderByFechaDesc(pageable)
                .map(MovimientoResponse::from);
    }
    @Transactional(readOnly = true)
    public List<MovimientoResponse> historialProducto(Long productoId) {
        return inventarioRepo.findByProductoIdOrderByFechaDesc(productoId)
                .stream().map(MovimientoResponse::from).toList();
    }
}
