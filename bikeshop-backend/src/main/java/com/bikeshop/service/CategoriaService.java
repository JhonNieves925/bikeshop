package com.bikeshop.service;

import com.bikeshop.dto.categoria.CategoriaRequest;
import com.bikeshop.dto.categoria.CategoriaResponse;
import com.bikeshop.entity.Categoria;
import com.bikeshop.entity.Producto;
import com.bikeshop.repository.CategoriaRepository;
import com.bikeshop.repository.ProductoRepository;
import com.bikeshop.util.SlugGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepo;
    private final ProductoRepository productoRepo;
    private final ImagenService imagenService;
    private final ProductoService productoService;

    public CategoriaService(CategoriaRepository categoriaRepo,
                            ProductoRepository productoRepo,
                            ImagenService imagenService,
                            @Lazy ProductoService productoService) {
        this.categoriaRepo = categoriaRepo;
        this.productoRepo = productoRepo;
        this.imagenService = imagenService;
        this.productoService = productoService;
    }

    // ─── PÚBLICO ────────────────────────────────────────────────────────────

    public List<CategoriaResponse> listarActivas() {
        return categoriaRepo.findByActivaTrueOrderByOrdenAsc()
                .stream().map(CategoriaResponse::from).toList();
    }

    public CategoriaResponse buscarPorSlug(String slug) {
        Categoria c = categoriaRepo.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada: " + slug));
        return CategoriaResponse.from(c);
    }

    // ─── ADMIN ──────────────────────────────────────────────────────────────

    public List<CategoriaResponse> listarTodas() {
        return categoriaRepo.findAll().stream().map(CategoriaResponse::from).toList();
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? request.getSlug()
                : SlugGenerator.generar(request.getNombre());

        if (categoriaRepo.existsBySlug(slug)) {
            throw new IllegalArgumentException("Ya existe una categoría con el slug: " + slug);
        }
        Categoria c = Categoria.builder()
                .nombre(request.getNombre())
                .slug(slug)
                .descripcion(request.getDescripcion())
                .iconoUrl(request.getIconoUrl())
                .orden(request.getOrden())
                .activa(request.isActiva())
                .build();
        return CategoriaResponse.from(categoriaRepo.save(c));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria c = categoriaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        String nuevoSlug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? request.getSlug()
                : c.getSlug();

        if (!c.getSlug().equals(nuevoSlug) && categoriaRepo.existsBySlug(nuevoSlug)) {
            throw new IllegalArgumentException("Ya existe una categoría con el slug: " + nuevoSlug);
        }

        c.setNombre(request.getNombre());
        c.setSlug(nuevoSlug);
        c.setDescripcion(request.getDescripcion());
        c.setIconoUrl(request.getIconoUrl());
        c.setOrden(request.getOrden());
        c.setActiva(request.isActiva());

        return CategoriaResponse.from(categoriaRepo.save(c));
    }

    @Transactional
    public CategoriaResponse subirImagen(Long id, MultipartFile archivo) {
        Categoria c = categoriaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        if (c.getIconoUrl() != null && c.getIconoUrl().startsWith("/uploads/")) {
            imagenService.eliminar(c.getIconoUrl());
        }

        String url = imagenService.guardar(archivo);
        c.setIconoUrl(url);
        return CategoriaResponse.from(categoriaRepo.save(c));
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria c = categoriaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        // Eliminar todos los productos de la categoría (con toda su info relacionada)
        List<Producto> productos = productoRepo.findByCategoriaId(id);
        for (Producto p : productos) {
            productoService.eliminarProductoInterno(p);
        }

        // Limpiar imagen de la categoría del disco
        if (c.getIconoUrl() != null && c.getIconoUrl().startsWith("/uploads/")) {
            imagenService.eliminar(c.getIconoUrl());
        }

        categoriaRepo.delete(c);
    }

    @Transactional
    public void toggleActiva(Long id) {
        Categoria c = categoriaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        c.setActiva(!Boolean.TRUE.equals(c.getActiva()));
        categoriaRepo.save(c);
    }
}
