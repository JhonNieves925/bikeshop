package com.bikeshop.dto.bicicleta;

import com.bikeshop.entity.Bicicleta;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BicicletaResponse {
    private Long id;
    private String marca;
    private String modelo;
    private String anio;
    private String color;
    private String tipo;
    private String numeroSerie;
    private String notas;
    private String fotoUrl;
    private LocalDateTime creadoEn;

    public static BicicletaResponse from(Bicicleta b) {
        BicicletaResponse r = new BicicletaResponse();
        r.id = b.getId();
        r.marca = b.getMarca();
        r.modelo = b.getModelo();
        r.anio = b.getAnio();
        r.color = b.getColor();
        r.tipo = b.getTipo();
        r.numeroSerie = b.getNumeroSerie();
        r.notas = b.getNotas();
        r.fotoUrl = b.getFotoUrl();
        r.creadoEn = b.getCreadoEn();
        return r;
    }
}
