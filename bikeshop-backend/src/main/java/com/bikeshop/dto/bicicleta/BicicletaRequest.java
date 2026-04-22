package com.bikeshop.dto.bicicleta;

import lombok.Data;

@Data
public class BicicletaRequest {
    private String marca;
    private String modelo;
    private String anio;
    private String color;
    private String tipo;
    private String numeroSerie;
    private String notas;
}
