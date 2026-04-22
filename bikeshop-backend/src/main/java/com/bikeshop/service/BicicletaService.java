package com.bikeshop.service;

import com.bikeshop.dto.bicicleta.BicicletaRequest;
import com.bikeshop.dto.bicicleta.BicicletaResponse;
import com.bikeshop.dto.mantenimiento.MantenimientoResponse;
import com.bikeshop.entity.Bicicleta;
import com.bikeshop.entity.Cliente;
import com.bikeshop.repository.BicicletaRepository;
import com.bikeshop.repository.ClienteRepository;
import com.bikeshop.repository.MantenimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BicicletaService {

    private final BicicletaRepository bicicletaRepository;
    private final ClienteRepository clienteRepository;
    private final MantenimientoRepository mantenimientoRepository;

    public List<BicicletaResponse> listarPorCliente(Long clienteId) {
        return bicicletaRepository.findByClienteIdOrderByCreadoEnDesc(clienteId)
                .stream().map(BicicletaResponse::from).toList();
    }

    @Transactional
    public BicicletaResponse crear(Long clienteId, BicicletaRequest req) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Bicicleta b = Bicicleta.builder()
                .cliente(cliente)
                .marca(req.getMarca())
                .modelo(req.getModelo())
                .anio(req.getAnio())
                .color(req.getColor())
                .tipo(req.getTipo())
                .numeroSerie(req.getNumeroSerie())
                .notas(req.getNotas())
                .build();

        return BicicletaResponse.from(bicicletaRepository.save(b));
    }

    @Transactional
    public BicicletaResponse actualizar(Long clienteId, Long bicicletaId, BicicletaRequest req) {
        Bicicleta b = bicicletaRepository.findById(bicicletaId)
                .orElseThrow(() -> new RuntimeException("Bicicleta no encontrada"));

        if (!b.getCliente().getId().equals(clienteId)) {
            throw new RuntimeException("No autorizado");
        }

        b.setMarca(req.getMarca());
        b.setModelo(req.getModelo());
        b.setAnio(req.getAnio());
        b.setColor(req.getColor());
        b.setTipo(req.getTipo());
        b.setNumeroSerie(req.getNumeroSerie());
        b.setNotas(req.getNotas());

        return BicicletaResponse.from(bicicletaRepository.save(b));
    }

    @Transactional
    public void eliminar(Long clienteId, Long bicicletaId) {
        Bicicleta b = bicicletaRepository.findById(bicicletaId)
                .orElseThrow(() -> new RuntimeException("Bicicleta no encontrada"));

        if (!b.getCliente().getId().equals(clienteId)) {
            throw new RuntimeException("No autorizado");
        }

        bicicletaRepository.delete(b);
    }

    @Transactional(readOnly = true)
    public List<MantenimientoResponse> historialPorBicicleta(Long clienteId, Long bicicletaId) {
        Bicicleta b = bicicletaRepository.findById(bicicletaId)
                .orElseThrow(() -> new RuntimeException("Bicicleta no encontrada"));

        if (!b.getCliente().getId().equals(clienteId)) {
            throw new RuntimeException("No autorizado");
        }

        return mantenimientoRepository.findByBicicletaIdOrderByFechaDesc(bicicletaId)
                .stream().map(MantenimientoResponse::from).toList();
    }

    // Para uso del admin: listar todas las bicis de un cliente
    public List<BicicletaResponse> listarPorClienteAdmin(Long clienteId) {
        return bicicletaRepository.findByClienteIdOrderByCreadoEnDesc(clienteId)
                .stream().map(BicicletaResponse::from).toList();
    }
}
