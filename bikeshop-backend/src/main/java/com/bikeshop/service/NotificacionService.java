package com.bikeshop.service;

import com.bikeshop.entity.NotificacionLog;
import com.bikeshop.repository.NotificacionLogRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final JavaMailSender mailSender;
    private final NotificacionLogRepository logRepo;

    @Value("${twilio.account-sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token}")
    private String twilioAuthToken;

    @Value("${twilio.whatsapp-from}")
    private String twilioWhatsappFrom;

    @Value("${app.notificaciones.email-remitente}")
    private String emailRemitente;

    @Value("${app.notificaciones.whatsapp-habilitado}")
    private boolean whatsappHabilitado;

    @PostConstruct
    public void initTwilio() {
        if (whatsappHabilitado) {
            Twilio.init(twilioAccountSid, twilioAuthToken);
        }
    }

    // ─── CONFIRMACIÓN DE PEDIDO ──────────────────────────────────────────────

    @Async
    public void notificarPedidoCreado(Long pedidoId, String nombreCliente,
                                       String email, String celular, String total) {
        String asunto = "BikeShop - Pedido #" + pedidoId + " recibido";
        String mensaje = """
                Hola %s,

                Recibimos tu pedido #%d por un total de $%s.

                Estado actual: PENDIENTE
                Te notificaremos cuando sea confirmado y despachado.

                Gracias por comprar en BikeShop 🚴
                """.formatted(nombreCliente, pedidoId, total);

        enviarEmail(email, asunto, mensaje, pedidoId, NotificacionLog.TipoReferencia.PEDIDO);

        if (celular != null && !celular.isBlank()) {
            String mensajeWa = "🚴 *BikeShop* - Pedido #" + pedidoId + " recibido.\n"
                    + "Total: $" + total + "\n"
                    + "Te avisamos cuando sea despachado. ¡Gracias " + nombreCliente + "!";
            enviarWhatsApp(celular, mensajeWa, pedidoId, NotificacionLog.TipoReferencia.PEDIDO);
        }
    }

    @Async
    public void notificarPedidoDespachado(Long pedidoId, String nombreCliente,
                                           String email, String celular) {
        String asunto = "BikeShop - Pedido #" + pedidoId + " despachado 🚚";
        String mensaje = """
                Hola %s,

                Tu pedido #%d ya fue despachado y está en camino.

                Gracias por tu compra en BikeShop 🚴
                """.formatted(nombreCliente, pedidoId);

        enviarEmail(email, asunto, mensaje, pedidoId, NotificacionLog.TipoReferencia.PEDIDO);

        if (celular != null && !celular.isBlank()) {
            String mensajeWa = "🚚 *BikeShop* - Tu pedido #" + pedidoId + " fue despachado. ¡Va en camino " + nombreCliente + "!";
            enviarWhatsApp(celular, mensajeWa, pedidoId, NotificacionLog.TipoReferencia.PEDIDO);
        }
    }

    // ─── CONFIRMACIÓN DE MANTENIMIENTO ───────────────────────────────────────

    @Async
    public void notificarCitaAgendada(Long mantenimientoId, String nombreCliente,
                                       String email, String celular,
                                       LocalDate fecha, LocalTime hora) {
        String fechaStr = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String horaStr = hora.format(DateTimeFormatter.ofPattern("hh:mm a"));

        String asunto = "BikeShop - Cita de mantenimiento confirmada";
        String mensaje = """
                Hola %s,

                Tu cita de mantenimiento ha sido agendada exitosamente.

                📅 Fecha: %s
                🕐 Hora: %s

                Por favor llega a tiempo. Si necesitas cancelar, contáctanos.

                ¡Hasta pronto! - BikeShop 🚴
                """.formatted(nombreCliente, fechaStr, horaStr);

        enviarEmail(email, asunto, mensaje, mantenimientoId, NotificacionLog.TipoReferencia.MANTENIMIENTO);

        if (celular != null && !celular.isBlank()) {
            String mensajeWa = "🔧 *BikeShop* - Cita confirmada para *" + nombreCliente + "*\n"
                    + "📅 " + fechaStr + " a las " + horaStr + "\n"
                    + "¡Te esperamos!";
            enviarWhatsApp(celular, mensajeWa, mantenimientoId, NotificacionLog.TipoReferencia.MANTENIMIENTO);
        }
    }

    @Async
    public void notificarRecordatorioCita(Long mantenimientoId, String nombreCliente,
                                           String email, String celular,
                                           LocalDate fecha, LocalTime hora) {
        String fechaStr = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String horaStr = hora.format(DateTimeFormatter.ofPattern("hh:mm a"));

        String asunto = "BikeShop - Recordatorio: tu cita es mañana";
        String mensaje = """
                Hola %s,

                Te recordamos que mañana tienes una cita de mantenimiento.

                📅 Fecha: %s
                🕐 Hora: %s

                ¡Te esperamos! - BikeShop 🚴
                """.formatted(nombreCliente, fechaStr, horaStr);

        enviarEmail(email, asunto, mensaje, mantenimientoId, NotificacionLog.TipoReferencia.RECORDATORIO);

        if (celular != null && !celular.isBlank()) {
            String mensajeWa = "⏰ *BikeShop* - Recordatorio: mañana " + fechaStr + " a las " + horaStr
                    + " tienes cita de mantenimiento, " + nombreCliente + ". ¡Te esperamos!";
            enviarWhatsApp(celular, mensajeWa, mantenimientoId, NotificacionLog.TipoReferencia.RECORDATORIO);
        }
    }

    // ─── EMAIL SIMPLE (sin log de referencia) ────────────────────────────────

    @Async
    public void enviarEmailSimple(String destinatario, String asunto, String mensaje) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(emailRemitente);
            mail.setTo(destinatario);
            mail.setSubject(asunto);
            mail.setText(mensaje);
            mailSender.send(mail);
        } catch (Exception e) {
            log.error("Error enviando email a {}: {}", destinatario, e.getMessage());
        }
    }

    // ─── MÉTODOS INTERNOS ────────────────────────────────────────────────────

    private void enviarEmail(String destinatario, String asunto, String mensaje,
                              Long referenciaId, NotificacionLog.TipoReferencia tipo) {
        NotificacionLog log = NotificacionLog.builder()
                .referenciaId(referenciaId)
                .tipoReferencia(tipo)
                .destinatario(destinatario)
                .canal(NotificacionLog.Canal.EMAIL)
                .asunto(asunto)
                .mensaje(mensaje)
                .build();

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(emailRemitente);
            mail.setTo(destinatario);
            mail.setSubject(asunto);
            mail.setText(mensaje);
            mailSender.send(mail);

            log.setEstado(NotificacionLog.Estado.ENVIADO);
        } catch (Exception e) {
            log.setEstado(NotificacionLog.Estado.FALLIDO);
            log.setErrorDetalle(e.getMessage());
            log.setIntentos(1);
        }

        logRepo.save(log);
    }

    private void enviarWhatsApp(String celular, String mensaje,
                                 Long referenciaId, NotificacionLog.TipoReferencia tipo) {
        if (!whatsappHabilitado) return;

        // Normalizar número colombiano: 3XX XXX XXXX → whatsapp:+573XXXXXXXXX
        String numero = normalizarCelular(celular);

        NotificacionLog log = NotificacionLog.builder()
                .referenciaId(referenciaId)
                .tipoReferencia(tipo)
                .destinatario(numero)
                .canal(NotificacionLog.Canal.WHATSAPP)
                .mensaje(mensaje)
                .build();

        try {
            Message.creator(
                    new PhoneNumber(numero),
                    new PhoneNumber(twilioWhatsappFrom),
                    mensaje
            ).create();

            log.setEstado(NotificacionLog.Estado.ENVIADO);
        } catch (Exception e) {
            log.setEstado(NotificacionLog.Estado.FALLIDO);
            log.setErrorDetalle(e.getMessage());
            log.setIntentos(1);
        }

        logRepo.save(log);
    }

    private String normalizarCelular(String celular) {
        String limpio = celular.replaceAll("[^0-9]", "");
        if (limpio.startsWith("57")) return "whatsapp:+" + limpio;
        if (limpio.startsWith("3") && limpio.length() == 10) return "whatsapp:+57" + limpio;
        return "whatsapp:+" + limpio;
    }
}
