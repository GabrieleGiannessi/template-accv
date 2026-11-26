package com.giannesuite.squadtemplate.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Parametri per creare l’anteprima di una figurina.")
public record FigurinaGraphicRequest(
        @Schema(description = "Formato: story, card o post.", example = "card") String format,
        String player_key,
        String photo_path,
        @Schema(description = "Nome del giocatore.", example = "Mario Rossi") String player_name,
        @Schema(description = "Ruolo mostrato nella figurina.", example = "Attaccante") String role,
        @Schema(description = "Numero di maglia.", example = "10") String jersey_number,
        @Schema(description = "Nome della squadra.", example = "A.C.C.V.") String team_name,
        String team_logo_path,
        @Schema(description = "Indica se mostrare il banner.", example = "true") Boolean with_banner) { }
