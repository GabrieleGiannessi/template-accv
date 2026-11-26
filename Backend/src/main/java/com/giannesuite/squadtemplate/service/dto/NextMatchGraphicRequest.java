package com.giannesuite.squadtemplate.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informazioni della prossima partita e opzioni grafiche.")
public record NextMatchGraphicRequest(
                                      @Schema(description = "Formato immagine, ad esempio 9:16 o 4:5.", example = "9:16") String format,
                                      String bg_path, String emotion, Double contrast_factor,
                                      Boolean remove_contrast,
                                      @Schema(description = "Nome della competizione o del torneo.", example = "Coppa Italia") String tournament,
                                      @Schema(description = "Località della partita.", example = "Venezia") String location,
                                      @Schema(description = "Campo o impianto sportivo.", example = "Campo Comunale") String field_name,
                                      TeamGraphicRequest home_team, TeamGraphicRequest away_team) {
}
