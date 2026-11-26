package com.giannesuite.squadtemplate.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Payload compatibile con il flusso di anteprima MVP esistente. */
@Schema(description = "Statistiche del giocatore e opzioni per la grafica MVP.")
public record MvpGraphicRequest(
        @Schema(description = "Formato immagine, ad esempio 4:5 o 9:16.", example = "4:5")
        String format,
        String bg_path,
        String emotion,
        Double contrast_factor,
        Boolean remove_contrast,
        @Schema(description = "Nome del giocatore.", example = "Mario Rossi")
        String player_name,
        @Schema(description = "Numero di maglia.", example = "10")
        String jersey_number,
        String position,
        @Schema(description = "Gol segnati.", example = "2")
        Integer goals,
        @Schema(description = "Assist effettuati.", example = "1")
        Integer assists,
        Integer saves,
        String rating,
        String match_opponent) {
}
