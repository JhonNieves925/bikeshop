package com.bikeshop.service;

import com.bikeshop.dto.novedad.NovedadRequest;
import com.bikeshop.dto.novedad.NovedadResponse;
import com.bikeshop.entity.Novedad;
import com.bikeshop.entity.NovedadImagen;
import com.bikeshop.repository.NovedadImagenRepository;
import com.bikeshop.repository.NovedadRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NovedadService {

    private final NovedadRepository novedadRepo;
    private final NovedadImagenRepository imagenGaleriaRepo;
    private final ImagenService imagenService;

    // ─── PÚBLICO ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<NovedadResponse> listarActivas() {
        return novedadRepo.findByActivaTrueOrderByCreadoEnDesc()
                .stream().map(NovedadResponse::from).toList();
    }

    // ─── ADMIN ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<NovedadResponse> listarTodas(Pageable pageable) {
        return novedadRepo.findAllByOrderByCreadoEnDesc(pageable)
                .map(NovedadResponse::from);
    }

    @Transactional
    public NovedadResponse crear(NovedadRequest request) {
        Novedad n = Novedad.builder()
                .titulo(request.getTitulo())
                .resumen(request.getResumen())
                .contenido(request.getContenido())
                .imagenUrl(request.getImagenUrl())
                .tipo(request.getTipo())
                .activa(request.isActiva())
                .destacada(request.isDestacada())
                .build();
        return NovedadResponse.from(novedadRepo.save(n));
    }

    @Transactional
    public NovedadResponse actualizar(Long id, NovedadRequest request) {
        Novedad n = novedadRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Novedad no encontrada"));

        n.setTitulo(request.getTitulo());
        n.setResumen(request.getResumen());
        n.setContenido(request.getContenido());
        n.setImagenUrl(request.getImagenUrl());
        n.setTipo(request.getTipo());
        n.setActiva(request.isActiva());
        n.setDestacada(request.isDestacada());

        return NovedadResponse.from(novedadRepo.save(n));
    }

    @Transactional
    public void eliminar(Long id) {
        Novedad n = novedadRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Novedad no encontrada"));

        // Limpiar portada del disco
        if (n.getImagenUrl() != null && n.getImagenUrl().startsWith("/uploads/")) {
            imagenService.eliminar(n.getImagenUrl());
        }
        // Limpiar galería del disco
        n.getGaleria().forEach(img -> {
            if (img.getImagenUrl() != null && img.getImagenUrl().startsWith("/uploads/")) {
                imagenService.eliminar(img.getImagenUrl());
            }
        });
        novedadRepo.delete(n);
    }

    @Transactional
    public NovedadResponse subirPortada(Long id, MultipartFile archivo) {
        Novedad n = novedadRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Novedad no encontrada"));

        if (n.getImagenUrl() != null && n.getImagenUrl().startsWith("/uploads/")) {
            imagenService.eliminar(n.getImagenUrl());
        }
        n.setImagenUrl(imagenService.guardar(archivo));
        return NovedadResponse.from(novedadRepo.save(n));
    }

    // ─── GALERÍA ─────────────────────────────────────────────────────────────

    @Transactional
    public NovedadResponse agregarFotoGaleria(Long novedadId, MultipartFile archivo) {
        Novedad n = novedadRepo.findById(novedadId)
                .orElseThrow(() -> new EntityNotFoundException("Novedad no encontrada"));

        String url = imagenService.guardar(archivo);
        int orden = n.getGaleria().size();

        NovedadImagen img = NovedadImagen.builder()
                .novedad(n)
                .imagenUrl(url)
                .orden(orden)
                .build();
        imagenGaleriaRepo.save(img);

        // recargar para devolver galería completa
        return NovedadResponse.from(novedadRepo.findById(novedadId).get());
    }

    @Transactional
    public void eliminarFotoGaleria(Long novedadId, Long imagenId) {
        NovedadImagen img = imagenGaleriaRepo.findById(imagenId)
                .orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada"));

        if (!img.getNovedad().getId().equals(novedadId)) {
            throw new IllegalArgumentException("La imagen no pertenece a esta novedad");
        }
        if (img.getImagenUrl() != null && img.getImagenUrl().startsWith("/uploads/")) {
            imagenService.eliminar(img.getImagenUrl());
        }
        imagenGaleriaRepo.delete(img);
    }
}
