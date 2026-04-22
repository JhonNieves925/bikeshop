package com.bikeshop.service;

import com.bikeshop.dto.cupon.*;
import com.bikeshop.entity.Cupon;
import com.bikeshop.repository.CuponRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuponService {

    private final CuponRepository cuponRepo;

    // ── ADMIN ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CuponResponse> listar() {
        return cuponRepo.findAll().stream()
                .map(CuponResponse::from)
                .toList();
    }

    @Transactional
    public CuponResponse crear(CuponRequest req) {
        String codigo = req.getCodigo().toUpperCase().trim();
        if (cuponRepo.existsByCodigo(codigo)) {
            throw new IllegalArgumentException("Ya existe un cupón con ese código");
        }
        Cupon cupon = Cupon.builder()
                .codigo(codigo)
                .tipo(req.getTipo())
                .descuento(req.getDescuento())
                .fechaExpira(req.getFechaExpira())
                .usosMax(req.getUsosMax())
                .build();
        return CuponResponse.from(cuponRepo.save(cupon));
    }

    @Transactional
    public CuponResponse toggleActivo(Long id) {
        Cupon c = cuponRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cupón no encontrado"));
        c.setActivo(!c.isActivo());
        return CuponResponse.from(cuponRepo.save(c));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!cuponRepo.existsById(id)) throw new EntityNotFoundException("Cupón no encontrado");
        cuponRepo.deleteById(id);
    }

    // ── PÚBLICO / CHECKOUT ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ValidarCuponResponse validar(ValidarCuponRequest req) {
        String codigo = req.getCodigo().toUpperCase().trim();
        Cupon c = cuponRepo.findByCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Cupón no válido"));

        if (!c.isActivo())
            throw new IllegalArgumentException("Este cupón está desactivado");

        if (c.getFechaExpira() != null && c.getFechaExpira().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Este cupón ya expiró");

        if (c.getUsosMax() != null && c.getUsosActuales() >= c.getUsosMax())
            throw new IllegalArgumentException("Este cupón ya alcanzó el límite de usos");

        BigDecimal subtotal = req.getSubtotal();
        BigDecimal monto;

        if (c.getTipo() == Cupon.Tipo.PORCENTAJE) {
            monto = subtotal.multiply(c.getDescuento())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            monto = c.getDescuento().min(subtotal); // no puede superar el subtotal
        }

        BigDecimal total = subtotal.subtract(monto).max(BigDecimal.ZERO);

        return ValidarCuponResponse.builder()
                .codigo(c.getCodigo())
                .tipo(c.getTipo())
                .descuento(c.getDescuento())
                .montoDescuento(monto)
                .totalConDescuento(total)
                .build();
    }

    /** Incrementa el contador de usos — llamado al confirmar pedido */
    @Transactional
    public void registrarUso(String codigo) {
        cuponRepo.findByCodigo(codigo.toUpperCase().trim())
                .ifPresent(c -> {
                    c.setUsosActuales(c.getUsosActuales() + 1);
                    cuponRepo.save(c);
                });
    }
}
