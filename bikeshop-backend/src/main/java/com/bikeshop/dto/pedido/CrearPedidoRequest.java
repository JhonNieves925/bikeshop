package com.bikeshop.dto.pedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CrearPedidoRequest {

    // Datos del comprador (siempre requeridos, con o sin cuenta)
    @NotBlank(message = "El nombre es obligatorio")
    private String nombreCliente;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String emailCliente;

    @NotBlank(message = "El celular es obligatorio")
    private String celularCliente;

    @NotBlank(message = "La dirección de entrega es obligatoria")
    private String direccionEntrega;

    @NotBlank(message = "La ciudad de entrega es obligatoria")
    private String ciudadEntrega;
    
    @NotBlank(message = "El departamento de entrega es obligatorio")
    private String departamentoEntrega;

    private String notasEntrega;

    // Items del carrito
    @NotEmpty(message = "El pedido debe tener al menos un producto")
    @Valid
    private List<PedidoItemRequest> items;

    /** Código de cupón aplicado (opcional) */
    private String cuponCodigo;
}
