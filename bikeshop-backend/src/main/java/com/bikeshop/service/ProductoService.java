package com.bikeshop.service;

import com.bikeshop.dto.producto.*;
import com.bikeshop.dto.produto.ProductoTallaRequest;
import com.bikeshop.entity.Categoria;
import com.bikeshop.entity.Producto;
import com.bikeshop.entity.ProductoImagen;
import com.bikeshop.entity.ProdutoTalla;
import com.bikeshop.repository.CategoriaRepository;
import com.bikeshop.repository.FacturaItemRepository;
import com.bikeshop.repository.InventarioMovimientoRepository;
import com.bikeshop.repository.MantenimientoRepuestoRepository;
import com.bikeshop.repository.PedidoItemRepository;
import com.bikeshop.repository.ProductoImagenRepository;
import com.bikeshop.repository.ProductoRepository;
import com.bikeshop.repository.ProdutoTallaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepo;
    private final ProductoImagenRepository imagenRepo;
    private final CategoriaRepository categoriaRepo;
    private final ImagenService imagenService;
    private final ProdutoTallaRepository tallaRepo;
    private final MantenimientoRepuestoRepository mantenimientoRepuestoRepo;
    private final InventarioMovimientoRepository inventarioMovimientoRepo;
    private final PedidoItemRepository pedidoItemRepo;
    private final FacturaItemRepository facturaItemRepo;

    // ─── PÚBLICO ────────────────────────────────────────────────────────────

    public Page<ProductoResumenResponse> listarPorCategoria(Long categoriaId, Pageable pageable) {
        return productoRepo.findByCategoriaIdAndActivoTrue(categoriaId, pageable)
                .map(p -> {
                    List<ProductoImagen> imagenes = imagenRepo.findByProductoIdOrderByOrdenAsc(p.getId());
                    List<ProdutoTalla> tallas = tallaRepo.findByProductoId(p.getId());
                    return ProductoResumenResponse.from(p, imagenes, tallas);
                });
    }

    public Page<ProductoResumenResponse> listarTodosActivos(Pageable pageable) {
        return productoRepo.findByActivoTrue(pageable)
                .map(p -> {
                    List<ProductoImagen> imagenes = imagenRepo.findByProductoIdOrderByOrdenAsc(p.getId());
                    List<ProdutoTalla> tallas = tallaRepo.findByProductoId(p.getId());
                    return ProductoResumenResponse.from(p, imagenes, tallas);
                });
    }

    public Page<ProductoResumenResponse> buscarActivos(String nombre, Long categoriaId,
                                                        BigDecimal precioMin, BigDecimal precioMax,
                                                        Pageable pageable) {
        String q = (nombre != null && !nombre.isBlank()) ? nombre.trim() : null;
        return productoRepo.buscarActivos(q, categoriaId, precioMin, precioMax, pageable)
                .map(p -> {
                    List<ProductoImagen> imagenes = imagenRepo.findByProductoIdOrderByOrdenAsc(p.getId());
                    List<ProdutoTalla> tallas = tallaRepo.findByProductoId(p.getId());
                    return ProductoResumenResponse.from(p, imagenes, tallas);
                });
    }

    public ProductoDetalleResponse buscarPorId(Long id) {
        Producto p = productoRepo.findById(id)
                .filter(prod -> Boolean.TRUE.equals(prod.getActivo()))
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        List<ProductoImagen> imagenes = imagenRepo.findByProductoIdOrderByOrdenAsc(id);
        List<ProdutoTalla> tallas = tallaRepo.findByProductoId(id);
        return ProductoDetalleResponse.from(p, imagenes, tallas);
    }

    // ─── ADMIN ──────────────────────────────────────────────────────────────

    public Page<ProductoResumenResponse> listarTodosAdmin(Pageable pageable) {
        return productoRepo.findAll(pageable)
                .map(p -> {
                    List<ProductoImagen> imagenes = imagenRepo.findByProductoIdOrderByOrdenAsc(p.getId());
                    List<ProdutoTalla> tallas = tallaRepo.findByProductoId(p.getId());
                    return ProductoResumenResponse.from(p, imagenes, tallas);
                });
    }

    public ProductoDetalleResponse buscarPorIdAdmin(Long id) {
        Producto p = productoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        List<ProductoImagen> imagenes = imagenRepo.findByProductoIdOrderByOrdenAsc(id);
        List<ProdutoTalla> tallas = tallaRepo.findByProductoId(id);
        return ProductoDetalleResponse.from(p, imagenes, tallas);
    }

    @Transactional
    public ProductoDetalleResponse crear(ProductoRequest request) {
        Categoria categoria = null;
        if (request.getCategoriaId() != null) {
            categoria = categoriaRepo.findById(request.getCategoriaId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        }

        boolean tieneTallas = request.getTallas() != null && !request.getTallas().isEmpty();
        int stockFinal = tieneTallas
                ? request.getTallas().stream().mapToInt(ProductoTallaRequest::getStock).sum()
                : request.getStock();

        Producto p = Producto.builder()
                .categoria(categoria)
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .stock(stockFinal)
                .stockMinimo(request.getStockMinimo())
                .activo(true)
                .build();

        p = productoRepo.save(p);

        List<ProdutoTalla> tallas = List.of();
        if (tieneTallas) {
            tallas = guardarTallas(p, request.getTallas());
        }

        return ProductoDetalleResponse.from(p, List.of(), tallas);
    }

    @Transactional
    public ProductoDetalleResponse actualizar(Long id, ProductoRequest request) {
        Producto p = productoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        Categoria categoria = categoriaRepo.findById(request.getCategoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        boolean tieneTallas = request.getTallas() != null && !request.getTallas().isEmpty();
        int stockFinal = tieneTallas
                ? request.getTallas().stream().mapToInt(ProductoTallaRequest::getStock).sum()
                : request.getStock();

        p.setCategoria(categoria);
        p.setNombre(request.getNombre());
        p.setDescripcion(request.getDescripcion());
        p.setPrecio(request.getPrecio());
        p.setStock(stockFinal);
        p.setStockMinimo(request.getStockMinimo());
        p.setActivo(request.isActivo());

        p = productoRepo.save(p);

        // Reemplazar tallas: borrar las existentes y guardar las nuevas
        tallaRepo.deleteByProductoId(id);
        List<ProdutoTalla> tallas = List.of();
        if (tieneTallas) {
            tallas = guardarTallas(p, request.getTallas());
        }

        List<ProductoImagen> imagenes = imagenRepo.findByProductoIdOrderByOrdenAsc(id);
        return ProductoDetalleResponse.from(p, imagenes, tallas);
    }

    @Transactional
    public void eliminar(Long id) {
        Producto p = productoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        eliminarProductoInterno(p);
    }

    /** Lógica de borrado completo, reutilizable desde CategoriaService */
    @Transactional
    public void eliminarProductoInterno(Producto p) {
        Long id = p.getId();

        // 1. Desreferenciar en tablas históricas (SET NULL)
        mantenimientoRepuestoRepo.clearProductoByProductoId(id);
        inventarioMovimientoRepo.clearProductoByProductoId(id);
        pedidoItemRepo.clearProductoByProductoId(id);
        facturaItemRepo.clearProductoByProductoId(id);

        // 2. Eliminar tallas
        tallaRepo.deleteByProductoId(id);

        // 3. Eliminar archivos de imágenes del disco
        List<ProductoImagen> imagenes = imagenRepo.findByProductoIdOrderByOrdenAsc(id);
        imagenes.forEach(img -> imagenService.eliminar(img.getUrl()));

        // 4. Eliminar el producto (cascade borra imagenes y tallas en BD)
        productoRepo.delete(p);
    }

    private List<ProdutoTalla> guardarTallas(Producto producto, List<ProductoTallaRequest> tallasReq) {
        return tallasReq.stream()
                .filter(t -> t.getTalla() != null && !t.getTalla().isBlank())
                .map(t -> tallaRepo.save(
                        ProdutoTalla.builder()
                                .producto(producto)
                                .talla(t.getTalla().trim().toUpperCase())
                                .stock(t.getStock())
                                .build()
                ))
                .toList();
    }

    @Transactional
    public ProductoImagenResponse agregarImagen(Long productoId, MultipartFile archivo, boolean esPrincipal) {
        Producto p = productoRepo.findById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        String url = imagenService.guardar(archivo);

        // Si esta imagen es principal, quitar el flag de las demás
        if (esPrincipal) {
            imagenRepo.findByProductoIdOrderByOrdenAsc(productoId).forEach(img -> {
                img.setEsPrincipal(false);
                imagenRepo.save(img);
            });
        }

        int siguienteOrden = imagenRepo.findByProductoIdOrderByOrdenAsc(productoId).size();

        ProductoImagen imagen = ProductoImagen.builder()
                .producto(p)
                .url(url)
                .esPrincipal(esPrincipal)
                .orden(siguienteOrden)
                .build();

        return ProductoImagenResponse.from(imagenRepo.save(imagen));
    }

    @Transactional
    public ProductoImagenResponse marcarPrincipal(Long productoId, Long imagenId) {
        ProductoImagen target = imagenRepo.findById(imagenId)
                .orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada"));
        if (!target.getProducto().getId().equals(productoId)) {
            throw new IllegalArgumentException("La imagen no pertenece a este producto");
        }
        imagenRepo.findByProductoIdOrderByOrdenAsc(productoId).forEach(img -> {
            img.setEsPrincipal(img.getId().equals(imagenId));
            imagenRepo.save(img);
        });
        return ProductoImagenResponse.from(target);
    }

    @Transactional
    public void eliminarImagen(Long productoId, Long imagenId) {
        ProductoImagen imagen = imagenRepo.findById(imagenId)
                .orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada"));

        if (!imagen.getProducto().getId().equals(productoId)) {
            throw new IllegalArgumentException("La imagen no pertenece a este producto");
        }

        imagenService.eliminar(imagen.getUrl());
        imagenRepo.delete(imagen);
    }

    @Transactional
    public void toggleActivo(Long id) {
        Producto p = productoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        p.setActivo(!Boolean.TRUE.equals(p.getActivo()));
        productoRepo.save(p);
    }

    public List<ProductoResumenResponse> listarConStockBajo() {
        return productoRepo.findProductosConStockBajo().stream()
                .map(p -> {
                    List<ProductoImagen> imagenes = imagenRepo.findByProductoIdOrderByOrdenAsc(p.getId());
                    List<ProdutoTalla> tallas = tallaRepo.findByProductoId(p.getId());
                    return ProductoResumenResponse.from(p, imagenes, tallas);
                }).toList();
    }
}
