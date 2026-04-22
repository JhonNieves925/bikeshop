package com.bikeshop.service;

import com.bikeshop.dto.empleado.*;
import com.bikeshop.entity.*;
import com.bikeshop.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpleadoService {

    private final UsuarioRepository usuarioRepo;
    private final EmpleadoActividadRepository actividadRepo;
    private final EmpleadoJornadaRepository jornadaRepo;
    private final EmpleadoPagoRepository pagoRepo;
    private final MantenimientoRepository mantenimientoRepo;
    private final PedidoRepository pedidoRepo;
    private final FacturaRepository facturaRepo;
    private final InventarioMovimientoRepository inventarioMovimientoRepo;
    private final DiaBloqueadoRepository diaBloqueadoRepo;
    private final PasswordEncoder passwordEncoder;

    // ─── CRUD EMPLEADOS ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EmpleadoResponse> listar() {
        return usuarioRepo.findByActivoTrue().stream()
                .map(EmpleadoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<EmpleadoResponse> listarTodos() {
        return usuarioRepo.findAll().stream().map(EmpleadoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public EmpleadoResponse buscarPorId(Long id) {
        return EmpleadoResponse.from(obtenerUsuario(id));
    }

    @Transactional
    public EmpleadoResponse crear(EmpleadoRequest request) {
        if (usuarioRepo.existsByNumeroDocumento(request.getDocumento())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese número de documento");
        }
        if (request.getContrasena() == null || request.getContrasena().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria al crear un empleado");
        }

        // Validar email único si viene informado
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && usuarioRepo.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        // Aceptar 'clave' como alias de 'contrasena'
        String pass = request.getContrasena() != null ? request.getContrasena() : request.getClave();

        Usuario u = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .numeroDocumento(request.getDocumento())
                .contrasenaHash(passwordEncoder.encode(pass))
                .rol(request.getRol() != null ? request.getRol() : Usuario.Rol.EMPLEADO)
                .telefono(request.getTelefono())
                .tipoPago(request.getTipoPago() != null ? request.getTipoPago() : Usuario.TipoPago.POR_SERVICIO)
                .tarifaHora(BigDecimal.ZERO)
                .tarifaServicio(BigDecimal.ZERO)
                .build();

        return EmpleadoResponse.from(usuarioRepo.save(u));
    }

    @Transactional
    public EmpleadoResponse actualizar(Long id, EmpleadoRequest request) {
        Usuario u = obtenerUsuario(id);

        // Validar documento único solo si cambió
        if (!u.getNumeroDocumento().equals(request.getDocumento())
                && usuarioRepo.existsByNumeroDocumento(request.getDocumento())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese número de documento");
        }

        // Validar email único solo si cambió
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equals(u.getEmail())
                && usuarioRepo.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        u.setNombre(request.getNombre());
        u.setApellido(request.getApellido());
        u.setEmail(request.getEmail());
        u.setNumeroDocumento(request.getDocumento());
        u.setTelefono(request.getTelefono());
        if (request.getRol() != null) u.setRol(request.getRol());
        if (request.getTipoPago() != null) u.setTipoPago(request.getTipoPago());

        // Aceptar 'clave' como alias de 'contrasena'
        String pass = request.getContrasena() != null ? request.getContrasena() : request.getClave();
        if (pass != null && !pass.isBlank()) {
            u.setContrasenaHash(passwordEncoder.encode(pass));
            u.setDebeCambiarClave(true);
        }

        return EmpleadoResponse.from(usuarioRepo.save(u));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!usuarioRepo.existsById(id)) {
            throw new EntityNotFoundException("Empleado no encontrado");
        }

        // 1. Desvincular referencias opcionales (SET NULL) en otras tablas
        mantenimientoRepo.clearAtendidoPorByUsuarioId(id);
        pedidoRepo.clearDespachadoPorByUsuarioId(id);
        facturaRepo.clearEmitidaPorByUsuarioId(id);
        inventarioMovimientoRepo.clearUsuarioByUsuarioId(id);
        actividadRepo.clearRegistradoPorByUsuarioId(id);

        // 2. Reasignar pagos que este usuario registró para OTROS empleados
        //    (registrado_por NOT NULL → se asigna al propio empleado pagado)
        pagoRepo.reassignRegistradoPorByUsuarioId(id);

        // 3. Eliminar registros con FK NOT NULL que pertenecen directamente al empleado
        diaBloqueadoRepo.deleteByCreadoPorId(id);
        actividadRepo.deleteByUsuarioId(id);
        jornadaRepo.deleteByUsuarioId(id);
        pagoRepo.deleteByUsuarioId(id);

        // 4. Eliminar el usuario
        usuarioRepo.deleteById(id);
    }

    // ─── JORNADAS ────────────────────────────────────────────────────────────

    @Transactional
    public JornadaResponse registrarEntrada(Long usuarioId, LocalDate fecha) {
        if (jornadaRepo.findByUsuarioIdAndFecha(usuarioId, fecha).isPresent()) {
            throw new IllegalStateException("Ya hay una jornada registrada para este empleado hoy");
        }
        Usuario u = obtenerUsuario(usuarioId);
        EmpleadoJornada j = EmpleadoJornada.builder()
                .usuario(u)
                .fecha(fecha)
                .horaEntrada(LocalTime.now())
                .build();
        return JornadaResponse.from(jornadaRepo.save(j));
    }

    @Transactional
    public JornadaResponse registrarSalida(Long usuarioId, LocalDate fecha) {
        EmpleadoJornada j = jornadaRepo.findByUsuarioIdAndFecha(usuarioId, fecha)
                .orElseThrow(() -> new EntityNotFoundException("No hay entrada registrada para este empleado hoy"));

        LocalTime salida = LocalTime.now();
        j.setHoraSalida(salida);

        // Calcular horas trabajadas
        long minutos = java.time.Duration.between(j.getHoraEntrada(), salida).toMinutes();
        BigDecimal horas = BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        j.setHorasTrabajadas(horas);

        return JornadaResponse.from(jornadaRepo.save(j));
    }

    @Transactional(readOnly = true)
    public List<JornadaResponse> jornadasEmpleado(Long usuarioId, LocalDate desde, LocalDate hasta) {
        return jornadaRepo.findByUsuarioIdAndFechaBetween(usuarioId, desde, hasta)
                .stream().map(JornadaResponse::from).toList();
    }

    // ─── ACTIVIDADES ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ActividadResponse> actividadesEmpleado(Long usuarioId, LocalDate desde, LocalDate hasta) {
        return actividadRepo.findByUsuarioIdAndFechaBetween(usuarioId, desde, hasta)
                .stream().map(ActividadResponse::from).toList();
    }

    // ─── PAGOS ───────────────────────────────────────────────────────────────

    /**
     * Genera el resumen de pago para un empleado en un período.
     * Calcula automáticamente según el tipo de pago del empleado:
     * - POR_HORA: suma horas de jornadas × tarifa_hora
     * - POR_SERVICIO: cuenta actividades × tarifa_servicio
     * - MIXTO: ambos
     */
    @Transactional
    public PagoResponse generarPago(GenerarPagoRequest request, Long adminId) {
        Usuario empleado = obtenerUsuario(request.getUsuarioId());
        Usuario admin = obtenerUsuario(adminId);

        // Obtener jornadas del período
        List<EmpleadoJornada> jornadas = jornadaRepo.findByUsuarioIdAndFechaBetween(
                request.getUsuarioId(), request.getFechaDesde(), request.getFechaHasta());

        BigDecimal totalHoras = jornadas.stream()
                .filter(j -> j.getHorasTrabajadas() != null)
                .map(EmpleadoJornada::getHorasTrabajadas)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Obtener actividades del período
        List<EmpleadoActividad> actividades = actividadRepo.findByUsuarioIdAndFechaBetween(
                request.getUsuarioId(), request.getFechaDesde(), request.getFechaHasta());

        int totalServicios = actividades.size();

        // Calcular montos según tipo de pago
        BigDecimal montoHoras = BigDecimal.ZERO;
        BigDecimal montoServicios = BigDecimal.ZERO;

        switch (empleado.getTipoPago()) {
            case POR_HORA -> montoHoras = totalHoras.multiply(empleado.getTarifaHora());
            case POR_SERVICIO -> montoServicios = empleado.getTarifaServicio()
                    .multiply(BigDecimal.valueOf(totalServicios));
            case MIXTO -> {
                montoHoras = totalHoras.multiply(empleado.getTarifaHora());
                montoServicios = empleado.getTarifaServicio()
                        .multiply(BigDecimal.valueOf(totalServicios));
            }
        }

        BigDecimal bonificaciones = request.getBonificaciones() != null ? request.getBonificaciones() : BigDecimal.ZERO;
        BigDecimal deducciones = request.getDeducciones() != null ? request.getDeducciones() : BigDecimal.ZERO;
        BigDecimal totalPago = montoHoras.add(montoServicios).add(bonificaciones).subtract(deducciones);

        EmpleadoPago pago = EmpleadoPago.builder()
                .usuario(empleado)
                .registradoPor(admin)
                .fechaDesde(request.getFechaDesde())
                .fechaHasta(request.getFechaHasta())
                .totalServicios(totalServicios)
                .totalHoras(totalHoras)
                .montoServicios(montoServicios)
                .montoHoras(montoHoras)
                .bonificaciones(bonificaciones)
                .deducciones(deducciones)
                .totalPago(totalPago)
                .observaciones(request.getObservaciones())
                .build();

        return PagoResponse.from(pagoRepo.save(pago));
    }

    @Transactional
    public PagoResponse marcarPagado(Long pagoId) {
        EmpleadoPago pago = pagoRepo.findById(pagoId)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado"));
        pago.setEstado(EmpleadoPago.Estado.PAGADO);
        pago.setPagadoEn(LocalDateTime.now());
        return PagoResponse.from(pagoRepo.save(pago));
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> pagosEmpleado(Long usuarioId) {
        return pagoRepo.findByUsuarioIdOrderByCreadoEnDesc(usuarioId)
                .stream().map(PagoResponse::from).toList();
    }

    // ─── HELPER ─────────────────────────────────────────────────────────────

    private Usuario obtenerUsuario(Long id) {
        return usuarioRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado"));
    }
}
