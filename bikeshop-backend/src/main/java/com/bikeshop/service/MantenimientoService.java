package com.bikeshop.service;

import com.bikeshop.dto.mantenimiento.*;
import com.bikeshop.entity.*;
import com.bikeshop.repository.*;
import com.bikeshop.repository.BicicletaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MantenimientoService {

    private final MantenimientoRepository mantenimientoRepo;
    private final MantenimientoRepuestoRepository repuestoRepo;
    private final HorarioTallerRepository horarioRepo;
    private final DiaBloqueadoRepository diaBloqueadoRepo;
    private final ClienteRepository clienteRepo;
    private final UsuarioRepository usuarioRepo;
    private final ProductoRepository productoRepo;
    private final InventarioMovimientoRepository inventarioRepo;
    private final NotificacionService notificacionService;
    private final BicicletaRepository bicicletaRepo;

    // ─── DISPONIBILIDAD ──────────────────────────────────────────────────────

    /**
     * Retorna los slots disponibles para una fecha dada.
     * Lógica:
     * 1. Verificar que la fecha no sea en el pasado
     * 2. Verificar que no sea día bloqueado
     * 3. Obtener horario del día de la semana (1=Lunes…7=Domingo)
     * 4. Generar todos los slots según duración
     * 5. Marcar los slots ya ocupados
     */
    public DisponibilidadResponse consultarDisponibilidad(LocalDate fecha) {
        if (fecha.isBefore(LocalDate.now())) {
            return DisponibilidadResponse.builder()
                    .fecha(fecha)
                    .diaDisponible(false)
                    .motivo("FECHA_PASADA")
                    .slots(List.of())
                    .build();
        }

        // Verificar día bloqueado
        if (diaBloqueadoRepo.existsByFecha(fecha)) {
            return DisponibilidadResponse.builder()
                    .fecha(fecha)
                    .diaDisponible(false)
                    .motivo("DIA_BLOQUEADO")
                    .slots(List.of())
                    .build();
        }

        // DayOfWeek en Java: MONDAY=1...SUNDAY=7 (iso value)
        byte diaSemana = (byte) fecha.getDayOfWeek().getValue();
        HorarioTaller horario = horarioRepo.findByDiaSemana(diaSemana).orElse(null);

        if (horario == null || !Boolean.TRUE.equals(horario.getActivo())) {
            return DisponibilidadResponse.builder()
                    .fecha(fecha)
                    .diaDisponible(false)
                    .motivo("TALLER_CERRADO")
                    .slots(List.of())
                    .build();
        }

        // Horas ya reservadas para esa fecha (solo activas, no canceladas)
        Set<LocalTime> horasOcupadas = mantenimientoRepo
                .findByFechaAndEstadoNot(fecha, Mantenimiento.Estado.CANCELADO)
                .stream()
                .map(Mantenimiento::getHoraInicio)
                .collect(Collectors.toSet());

        // Generar slots
        List<SlotDisponibleResponse> slots = new ArrayList<>();
        LocalTime cursor = horario.getHoraApertura();
        int duracion = horario.getDuracionSlotMinutos();

        while (cursor.isBefore(horario.getHoraCierre())) {
            boolean disponible = !horasOcupadas.contains(cursor);
            // Si es hoy, no mostrar slots pasados
            if (fecha.isEqual(LocalDate.now()) && !cursor.isAfter(LocalTime.now())) {
                disponible = false;
            }
            slots.add(new SlotDisponibleResponse(cursor, disponible));
            cursor = cursor.plusMinutes(duracion);
        }

        return DisponibilidadResponse.builder()
                .fecha(fecha)
                .diaDisponible(true)
                .slots(slots)
                .build();
    }

    /**
     * Retorna disponibilidad para un rango de fechas (para el calendario mensual).
     */
    public List<DisponibilidadResponse> consultarDisponibilidadRango(LocalDate desde, LocalDate hasta) {
        List<DisponibilidadResponse> resultado = new ArrayList<>();
        LocalDate cursor = desde;
        while (!cursor.isAfter(hasta)) {
            resultado.add(consultarDisponibilidad(cursor));
            cursor = cursor.plusDays(1);
        }
        return resultado;
    }

    // ─── AGENDAR (PÚBLICO) ───────────────────────────────────────────────────

    @Transactional
    public MantenimientoResponse agendarInvitado(AgendarMantenimientoRequest request) {
        return agendar(request, null);
    }

    @Transactional
    public MantenimientoResponse agendarCliente(AgendarMantenimientoRequest request, Long clienteId) {
        return agendar(request, clienteId);
    }

    private MantenimientoResponse agendar(AgendarMantenimientoRequest request, Long clienteId) {
        // Validar disponibilidad del slot
        DisponibilidadResponse disponibilidad = consultarDisponibilidad(request.getFecha());

        if (!disponibilidad.isDiaDisponible()) {
            throw new IllegalArgumentException("El día seleccionado no está disponible: " + disponibilidad.getMotivo());
        }

        boolean slotLibre = disponibilidad.getSlots().stream()
                .anyMatch(s -> s.getHora().equals(request.getHoraInicio()) && s.isDisponible());

        if (!slotLibre) {
            throw new IllegalArgumentException("El horario seleccionado ya no está disponible. Elige otro.");
        }

        Cliente cliente = clienteId != null ? clienteRepo.findById(clienteId).orElse(null) : null;

        // Vincular bicicleta registrada si viene en el request
        Bicicleta bicicleta = null;
        if (request.getBicicletaId() != null) {
            bicicleta = bicicletaRepo.findById(request.getBicicletaId()).orElse(null);

            // H4: verificar que la bicicleta pertenece al cliente autenticado.
            // Un cliente no debe poder agendar usando la bicicleta de otro.
            if (bicicleta != null && clienteId != null) {
                Long propietarioId = bicicleta.getCliente() != null
                        ? bicicleta.getCliente().getId() : null;
                if (!clienteId.equals(propietarioId)) {
                    throw new IllegalArgumentException(
                            "La bicicleta seleccionada no pertenece a tu cuenta.");
                }
            }

            // Auto-rellenar datos de la bici si están en el registro
            if (bicicleta != null) {
                if (request.getMarcaBicicleta() == null && bicicleta.getMarca() != null) {
                    request.setMarcaBicicleta(bicicleta.getMarca());
                }
                if (request.getModeloBicicleta() == null && bicicleta.getModelo() != null) {
                    request.setModeloBicicleta(bicicleta.getModelo());
                }
            }
        }

        Mantenimiento m = Mantenimiento.builder()
                .cliente(cliente)
                .bicicleta(bicicleta)
                .nombreCliente(request.getNombreCliente())
                .emailCliente(request.getEmailCliente())
                .celularCliente(request.getCelularCliente())
                .fecha(request.getFecha())
                .horaInicio(request.getHoraInicio())
                .tipoBicicleta(request.getTipoBicicleta())
                .marcaBicicleta(request.getMarcaBicicleta())
                .modeloBicicleta(request.getModeloBicicleta())
                .problemaReportado(request.getProblemaReportado())
                .build();

        m = mantenimientoRepo.save(m);
        m.setRepuestos(List.of());

        // Notificar al cliente
        notificacionService.notificarCitaAgendada(
                m.getId(),
                m.getNombreCliente(),
                m.getEmailCliente(),
                m.getCelularCliente(),
                m.getFecha(),
                m.getHoraInicio());

        return MantenimientoResponse.from(m);
    }

    // ─── GESTIÓN ADMIN ───────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<MantenimientoResponse> listarPorFecha(LocalDate fecha) {
        return mantenimientoRepo.findByFechaOrderByHoraInicioAsc(fecha)
                .stream().map(MantenimientoResponse::from).toList();
    }
    @Transactional(readOnly = true)
    public List<MantenimientoResponse> listarPorRango(LocalDate desde, LocalDate hasta) {
        return mantenimientoRepo.findByFechaBetweenOrderByFechaAscHoraInicioAsc(desde, hasta)
                .stream().map(MantenimientoResponse::from).toList();
    }
    @Transactional(readOnly = true)
    public Page<MantenimientoResponse> listarPorEstado(Mantenimiento.Estado estado, Pageable pageable) {
        return mantenimientoRepo.findByEstadoOrderByFechaAsc(estado, pageable)
                .map(MantenimientoResponse::from);
    }
    @Transactional(readOnly = true)
    public MantenimientoResponse detalle(Long id) {
        Mantenimiento m = mantenimientoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mantenimiento no encontrado"));
        return MantenimientoResponse.from(m);
    }
    @Transactional(readOnly = true)
    public List<MantenimientoResponse> misMantenimientos(Long clienteId) {
        return mantenimientoRepo.findByClienteIdOrderByFechaDesc(clienteId)
                .stream().map(MantenimientoResponse::from).toList();
    }

    /**
     * Actualiza el estado del mantenimiento.
     * PENDIENTE → EN_CURSO: se registra diagnóstico y mecánico asignado.
     * EN_CURSO → FINALIZADO: se registran trabajos, costos y repuestos usados.
     * Cualquier estado → CANCELADO.
     */
    @Transactional
    public MantenimientoResponse gestionar(Long id, GestionarMantenimientoRequest request) {
        Mantenimiento m = mantenimientoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mantenimiento no encontrado"));

        if (m.getEstado() == Mantenimiento.Estado.FINALIZADO ||
            m.getEstado() == Mantenimiento.Estado.CANCELADO) {
            throw new IllegalStateException("No se puede modificar un mantenimiento " + m.getEstado().name());
        }

        m.setEstado(request.getEstado());

        if (request.getEstado() == Mantenimiento.Estado.EN_CURSO) {
            if (request.getDiagnostico() != null) m.setDiagnostico(request.getDiagnostico());
            if (request.getAtendidoPorId() != null) {
                usuarioRepo.findById(request.getAtendidoPorId()).ifPresent(m::setAtendidoPor);
            }
        }

        if (request.getEstado() == Mantenimiento.Estado.FINALIZADO) {
            if (request.getTrabajosRealizados() != null) m.setTrabajosRealizados(request.getTrabajosRealizados());
            if (request.getNotasInternas() != null) m.setNotasInternas(request.getNotasInternas());
            if (request.getHoraFinReal() != null) m.setHoraFinReal(request.getHoraFinReal());

            BigDecimal costoManoObra = request.getCostoManoObra() != null ? request.getCostoManoObra() : BigDecimal.ZERO;
            m.setCostoManoObra(costoManoObra);

            // Procesar repuestos
            BigDecimal costoRepuestos = procesarRepuestos(m, request.getRepuestos());
            m.setCostoRepuestos(costoRepuestos);
            m.setCostoTotal(costoManoObra.add(costoRepuestos));
        }

        m = mantenimientoRepo.save(m);
        return MantenimientoResponse.from(m);
    }

    private BigDecimal procesarRepuestos(Mantenimiento mantenimiento, List<MantenimientoRepuestoRequest> repuestosReq) {
        if (repuestosReq == null || repuestosReq.isEmpty()) return BigDecimal.ZERO;

        BigDecimal totalRepuestos = BigDecimal.ZERO;

        for (MantenimientoRepuestoRequest repReq : repuestosReq) {
            Producto producto = productoRepo.findById(repReq.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + repReq.getProductoId()));

            if (producto.getStock() < repReq.getCantidad()) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para '" + producto.getNombre() + "'. Disponible: " + producto.getStock());
            }

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(repReq.getCantidad()));

            MantenimientoRepuesto repuesto = MantenimientoRepuesto.builder()
                    .mantenimiento(mantenimiento)
                    .producto(producto)
                    .cantidad(repReq.getCantidad())
                    .precioUnitario(producto.getPrecio())
                    .subtotal(subtotal)
                    .build();
            repuestoRepo.save(repuesto);

            // Descontar stock
            int stockAnterior = producto.getStock();
            producto.setStock(stockAnterior - repReq.getCantidad());
            productoRepo.save(producto);

            // Registrar movimiento de inventario
            inventarioRepo.save(InventarioMovimiento.builder()
                    .producto(producto)
                    .tipoMovimiento(InventarioMovimiento.TipoMovimiento.USO_MANTENIMIENTO)
                    .cantidad(-repReq.getCantidad())
                    .stockAnterior(stockAnterior)
                    .stockNuevo(producto.getStock())
                    .referenciaId(mantenimiento.getId())
                    .motivo("Repuesto en mantenimiento #" + mantenimiento.getId())
                    .build());

            totalRepuestos = totalRepuestos.add(subtotal);
        }

        return totalRepuestos;
    }

    // ─── HORARIOS Y DÍAS BLOQUEADOS ──────────────────────────────────────────

    @Transactional
    public void bloquearDia(LocalDate fecha, String motivo, Long adminId) {
        if (diaBloqueadoRepo.existsByFecha(fecha)) {
            throw new IllegalArgumentException("El día " + fecha + " ya está bloqueado");
        }
        Usuario admin = usuarioRepo.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        DiaBloqueado db = DiaBloqueado.builder()
                .fecha(fecha)
                .motivo(motivo)
                .creadoPor(admin)
                .build();
        diaBloqueadoRepo.save(db);
    }

    @Transactional
    public void desbloquearDia(LocalDate fecha) {
        diaBloqueadoRepo.findByFecha(fecha).ifPresent(diaBloqueadoRepo::delete);
    }
}
