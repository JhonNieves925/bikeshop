package com.bikeshop.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class ImagenService {

    private static final List<String> TIPOS_PERMITIDOS = List.of("image/jpeg", "image/png", "image/webp");

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.max-size-mb}")
    private int maxSizeMb;

    private Path uploadPath;

    @PostConstruct
    public void init() throws IOException {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadPath);
    }

    /**
     * Guarda el archivo en disco y retorna la URL pública relativa.
     * Ej: /uploads/images/abc123.jpg
     */
    public String guardar(MultipartFile archivo) {
        validar(archivo);

        String extension = obtenerExtension(archivo.getOriginalFilename());
        String nombreArchivo = UUID.randomUUID() + "." + extension;

        try {
            Path destino = uploadPath.resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la imagen: " + e.getMessage());
        }

        return "/uploads/images/" + nombreArchivo;
    }

    public void eliminar(String url) {
        if (url == null || url.isBlank()) return;
        // Extrae el nombre del archivo desde la URL: /uploads/images/abc.jpg -> abc.jpg
        String nombreArchivo = url.substring(url.lastIndexOf('/') + 1);
        try {
            Path archivo = uploadPath.resolve(nombreArchivo);
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            // No lanzamos excepción si falla el borrado físico
        }
    }

    private void validar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        // 1. Validar Content-Type declarado
        if (!TIPOS_PERMITIDOS.contains(archivo.getContentType())) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Use JPG, PNG o WebP");
        }
        // 2. Validar tamaño
        long maxBytes = (long) maxSizeMb * 1024 * 1024;
        if (archivo.getSize() > maxBytes) {
            throw new IllegalArgumentException("El archivo supera el tamaño máximo de " + maxSizeMb + "MB");
        }
        // 3. Validar magic bytes reales (no confiar solo en el Content-Type del cliente)
        // Un atacante podría enviar un .exe renombrado con Content-Type: image/jpeg
        try {
            byte[] header = archivo.getBytes().length >= 12
                    ? java.util.Arrays.copyOf(archivo.getBytes(), 12)
                    : archivo.getBytes();
            if (!esImagenValida(header)) {
                throw new IllegalArgumentException("El contenido del archivo no corresponde a una imagen válida");
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el archivo");
        }
    }

    /**
     * Verifica los magic bytes de los formatos permitidos:
     *  - JPEG: FF D8 FF
     *  - PNG:  89 50 4E 47 0D 0A 1A 0A
     *  - WebP: 52 49 46 46 ... 57 45 42 50
     */
    private boolean esImagenValida(byte[] h) {
        if (h.length < 4) return false;
        // JPEG
        if ((h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) return true;
        // PNG
        if ((h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') return true;
        // WebP: RIFF....WEBP
        if (h.length >= 12
                && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') return true;
        return false;
    }

    private String obtenerExtension(String nombreOriginal) {
        if (nombreOriginal != null && nombreOriginal.contains(".")) {
            return nombreOriginal.substring(nombreOriginal.lastIndexOf('.') + 1).toLowerCase();
        }
        return "jpg";
    }
}
