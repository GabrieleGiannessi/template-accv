package com.giannesuite.squadtemplate.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Marcatore e numero di reti da mostrare nella grafica.")
public record ScorerGraphicRequest(
        @Schema(description = "Nome del marcatore.", example = "Mario Rossi") String name,
        @Schema(description = "Numero di reti.", example = "1") Integer goals,
        @Schema(description = "Indica se la rete è un autogol.", example = "false") Boolean own_goal) { }
