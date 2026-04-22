package com.bikeshop.dto.factura;

import com.bikeshop.entity.Factura;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CrearFacturaRequest {

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String nombreCliente;

    private String emailCliente;

    // Canal: PRESENCIAL por defecto, también puede ser MANTENIMIENTO
    private Factura.Canal canal = Factura.Canal.PRESENCIAL;

    // Si el mantenimiento ya existe, se puede vincular
    private Long mantenimientoId;

    private BigDecimal descuento = BigDecimal.ZERO;
    private String observaciones;

    @NotEmpty(message = "La factura debe tener al menos un ítem")
    @Valid
    private List<FacturaItemRequest> items;
}
