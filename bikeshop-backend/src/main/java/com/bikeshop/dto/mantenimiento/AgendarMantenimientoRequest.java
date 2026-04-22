package com.bikeshop.dto.mantenimiento;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AgendarMantenimientoRequest {

    // Datos del cliente
    @NotBlank(message = "El nombre es obligatorio")
    private String nombreCliente;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String emailCliente;

    @NotBlank(message = "El celular es obligatorio")
    private String celularCliente;

    // Datos de la cita
    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime horaInicio;

    // Datos de la bicicleta
    @NotBlank(message = "El tipo de bicicleta es obligatorio")
    private String tipoBicicleta;

    private String marcaBicicleta;
    private String modeloBicicleta;

    @NotBlank(message = "Describe el problema o servicio que necesitas")
    private String problemaReportado;

    // Opcional: vincular con bicicleta registrada del cliente
    private Long bicicletaId;
}
