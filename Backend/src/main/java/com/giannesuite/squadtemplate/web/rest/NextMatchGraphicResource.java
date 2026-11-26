package com.giannesuite.squadtemplate.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.giannesuite.squadtemplate.utils.GraphicPreviewSupport;
import com.giannesuite.squadtemplate.rendering.NextMatchGraphicRenderer;
import com.giannesuite.squadtemplate.service.dto.NextMatchGraphicRequest;
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

@Tag(name = "Prossima partita", description = "Generazione della grafica della prossima partita.")
@RestController
@RequestMapping("/api/preview")
public class NextMatchGraphicResource {
    @Autowired
    private NextMatchGraphicRenderer renderer;

    @PostMapping("/next")
    @Operation(summary = "Genera grafica prossima partita")
    public RenderedGraphicDto render(@RequestBody(required = false) NextMatchGraphicRequest request) throws IOException {
        if (request == null) {
            request = new NextMatchGraphicRequest(null, null, null, null, null, null, null, null, null, null);
        }
        String format = request.format() == null || request.format().isBlank() ? "9:16" : request.format();
        return GraphicPreviewSupport.asPng(renderer.render(request), format);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> invalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", exception.getMessage()));
    }
}
