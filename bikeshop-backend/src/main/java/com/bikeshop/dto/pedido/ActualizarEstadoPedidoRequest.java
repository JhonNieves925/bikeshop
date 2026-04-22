package com.bikeshop.dto.pedido;

import com.bikeshop.entity.Pedido;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActualizarEstadoPedidoRequest {

    @NotNull(message = "El estado es obligatorio")
    private Pedido.Estado estado;
}
