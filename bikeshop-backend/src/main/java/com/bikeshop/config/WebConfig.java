package com.bikeshop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // uploadDir = "uploads/images"
        // La URL guardada es "/uploads/images/archivo.jpg"
        // Mapeamos /uploads/images/** directamente a la carpeta exacta donde se guardan los archivos
        String location = "file:" + Paths.get(uploadDir)
                .toAbsolutePath()
                .toString()
                .replace("\\", "/") + "/";

        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations(location);
    }
}
