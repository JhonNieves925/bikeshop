package com.bikeshop.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ImagenService {

    private static final List<String> TIPOS_PERMITIDOS = List.of("image/jpeg", "image/png", "image/webp");

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.max-size-mb}")
    private int maxSizeMb;

    @Value("${cloudinary.url:}")
    private String cloudinaryUrl;

    private Cloudinary cloudinary;
    private Path uploadPath;
    private boolean useCloudinary;

    @PostConstruct
    public void init() throws IOException {
        if (cloudinaryUrl != null && !cloudinaryUrl.isBlank()) {
            this.cloudinary = new Cloudinary(cloudinaryUrl);
            this.useCloudinary = true;
        } else {
            this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.uploadPath);
            this.useCloudinary = false;
        }
    }

    public String guardar(MultipartFile archivo) {
        validar(archivo);
        return useCloudinary ? guardarEnCloudinary(archivo) : guardarEnDisco(archivo);
    }

    public void eliminar(String url) {
        if (url == null || url.isBlank()) return;
        if (useCloudinary) {
            eliminarDeCloudinary(url);
        } else {
            eliminarDeDisco(url);
        }
    }

    private String guardarEnCloudinary(MultipartFile archivo) {
        try {
            Map resultado = cloudinary.uploader().upload(archivo.getBytes(), ObjectUtils.asMap(
                "folder", "bikeshop",
                "resource_type", "image"
            ));
            return (String) resultado.get("secure_url");
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo subir la imagen a Cloudinary: " + e.getMessage());
        }
    }

    private void eliminarDeCloudinary(String url) {
        try {
            // Extrae el public_id desde la URL de Cloudinary
            // URL format: https://res.cloudinary.com/cloud/image/upload/v123/bikeshop/filename.jpg
            String publicId = url.replaceAll(".*upload/(?:v\\d+/)?(.+)\\.[a-zA-Z]+$", "$1");
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            // No lanzamos excepción si falla el borrado
        }
    }

    private String guardarEnDisco(MultipartFile archivo) {
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

    private void eliminarDeDisco(String url) {
        if (url == null || url.isBlank()) return;
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
        if (!TIPOS_PERMITIDOS.contains(archivo.getContentType())) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Use JPG, PNG o WebP");
        }
        long maxBytes = (long) maxSizeMb * 1024 * 1024;
        if (archivo.getSize() > maxBytes) {
            throw new IllegalArgumentException("El archivo supera el tamaño máximo de " + maxSizeMb + "MB");
        }
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

    private boolean esImagenValida(byte[] h) {
        if (h.length < 4) return false;
        if ((h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) return true;
        if ((h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') return true;
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
