package com.bikeshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "horarios_taller")
public class HorarioTaller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dia_semana", nullable = false, unique = true)
    private Byte diaSemana;

    @Column(name = "hora_apertura", nullable = false)
    private LocalTime horaApertura;

    @Column(name = "hora_cierre", nullable = false)
    private LocalTime horaCierre;

    @Column(name = "duracion_slot_minutos")
    @Builder.Default
    private Integer duracionSlotMinutos = 60;

    @Column
    @Builder.Default
    private Boolean activo = true;
}
