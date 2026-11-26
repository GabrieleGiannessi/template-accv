package com.giannesuite.squadtemplate.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Risultato della generazione di un’anteprima grafica.")
public record RenderedGraphicDto(
        @Schema(description = "Esito della generazione.", example = "ok") String status,
        @Schema(description = "Anteprima PNG codificata come data URI.") String preview,
        @Schema(description = "Larghezza dell’immagine in pixel.", example = "1080") int width,
        @Schema(description = "Altezza dell’immagine in pixel.", example = "1920") int height,
        String format, String bg_path) {
}
