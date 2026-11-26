package com.giannesuite.squadtemplate.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.giannesuite.squadtemplate.rendering.FigurinaGraphicRenderer;
import com.giannesuite.squadtemplate.utils.GraphicPreviewSupport;
import com.giannesuite.squadtemplate.service.dto.FigurinaGraphicRequest;
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

@Tag(name = "Anteprima figurina", description = "Generazione grafica della figurina.")
@RestController
@RequestMapping("/api/preview")
public class FigurinaGraphicResource {
    @Autowired
    private FigurinaGraphicRenderer renderer;

    @PostMapping("/figurina")
    @Operation(summary = "Genera anteprima figurina")
    public RenderedGraphicDto render(@RequestBody(required = false) FigurinaGraphicRequest request) throws IOException {
        if (request == null) {
            request = new FigurinaGraphicRequest(null, null, null, null, null, null, null, null, null);
        }
        String format = request.format() == null || request.format().isBlank() ? "story" : request.format();
        var image = renderer.render(request);
        String responseFormat = format.equalsIgnoreCase("post") || format.equalsIgnoreCase("4:5") ? "4:5"
                : format.equalsIgnoreCase("card") ? "card" : "9:16";
        return GraphicPreviewSupport.asPng(image, responseFormat);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> invalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", exception.getMessage()));
    }
}
