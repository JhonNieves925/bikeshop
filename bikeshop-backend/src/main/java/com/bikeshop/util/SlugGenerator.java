package com.bikeshop.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class SlugGenerator {

    private static final Pattern NO_ALFANUMERICO = Pattern.compile("[^a-z0-9\\-]");
    private static final Pattern GUIONES_MULTIPLES = Pattern.compile("-{2,}");

    private SlugGenerator() {}

    /**
     * Convierte un texto en slug URL-friendly.
     * Ej: "Bicicletas de Montaña" → "bicicletas-de-montana"
     */
    public static String generar(String texto) {
        if (texto == null || texto.isBlank()) return "";

        String resultado = texto.trim().toLowerCase();

        // Eliminar acentos y caracteres especiales (ñ → n, á → a, etc.)
        resultado = Normalizer.normalize(resultado, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Reemplazar espacios y caracteres no permitidos por guión
        resultado = resultado.replace(" ", "-");
        resultado = NO_ALFANUMERICO.matcher(resultado).replaceAll("");
        resultado = GUIONES_MULTIPLES.matcher(resultado).replaceAll("-");

        // Eliminar guiones al inicio y al final
        return resultado.replaceAll("^-|-$", "");
    }
}
