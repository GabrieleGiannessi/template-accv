package com.giannesuite.squadtemplate.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dati di una squadra da inserire in una grafica.")
public record TeamGraphicRequest(
                                @Schema(description = "Nome della squadra.", example = "A.C.C.V.") String name,
                                @Schema(description = "Nome breve visualizzato nella grafica.", example = "ACCV") String short_name,
                                String logo_path,
                                @Schema(description = "Colore principale RGB.", example = "[16, 185, 129]") int[] primary_color,
                                @Schema(description = "Colore secondario RGB.", example = "[239, 68, 68]") int[] secondary_color,
                                int[] tertiary_color) {
    public String safeName(String fallback) { return name == null || name.isBlank() ? fallback : name; }
    public String safeShortName(String fallback) { return short_name == null || short_name.isBlank() ? fallback : short_name; }
}
