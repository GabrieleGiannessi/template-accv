package com.giannesuite.squadtemplate.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Dati di partita e opzioni grafiche per la generazione del risultato.")
public record MatchResultGraphicRequest(
                                        @Schema(description = "Formato immagine supportato, ad esempio 9:16, 4:5 o 1:1.", example = "9:16") String format,
                                        String bg_path, String emotion, Double contrast_factor,
                                        Boolean remove_contrast, Double bg_zoom, Double bg_x, Double bg_y,
                                        String graphic_variant, Boolean mix_team_colors,
                                        @Schema(description = "Reti della squadra di casa.", example = "2") Integer home_score,
                                        @Schema(description = "Reti della squadra ospite.", example = "1") Integer away_score,
                                        TeamGraphicRequest home_team, TeamGraphicRequest away_team,
                                        List<ScorerGraphicRequest> home_scorers, List<ScorerGraphicRequest> away_scorers,
                                        List<String> home_yellow_cards, List<String> away_yellow_cards,
                                        List<String> home_red_cards, List<String> away_red_cards,
                                        Boolean include_competition_info, String competition_description,
                                        String season_description, String competition_logo_path,
                                        String competition_key, String season_key) { }
