package com.giannesuite.squadtemplate.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.giannesuite.squadtemplate.rendering.MvpGraphicRenderer;
import com.giannesuite.squadtemplate.utils.GraphicPreviewSupport;
import com.giannesuite.squadtemplate.service.dto.MvpGraphicRequest;
import com.giannesuite.squadtemplate.service.dto.RenderedGraphicDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@Tag(name = "Anteprima MVP", description = "Generazione della grafica MVP.")
@RestController
@RequestMapping("/api/preview")
public class MvpGraphicResource {
    @Autowired
    private MvpGraphicRenderer renderer;

    @PostMapping("/mvp")
    @Operation(summary = "Genera anteprima MVP")
    public RenderedGraphicDto renderMvp(@RequestBody(required = false) MvpGraphicRequest request) throws IOException {
        if (request == null) {
            request = new MvpGraphicRequest(null, null, null, null, null, null, null, null, null, null, null, null, null);
        }
        String format = request.format() == null || request.format().isBlank() ? "4:5" : request.format();
        return GraphicPreviewSupport.asPng(renderer.render(request), format);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> invalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", exception.getMessage()));
    }
}
