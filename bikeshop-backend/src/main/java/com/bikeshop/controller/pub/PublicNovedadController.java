package com.bikeshop.controller.pub;

import com.bikeshop.dto.ApiResponse;
import com.bikeshop.dto.novedad.NovedadResponse;
import com.bikeshop.service.NovedadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/novedades")
@RequiredArgsConstructor
public class PublicNovedadController {

    private final NovedadService novedadService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NovedadResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(novedadService.listarActivas()));
    }
}
