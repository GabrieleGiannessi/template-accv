package com.giannesuite.squadtemplate.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Associazione fra una competizione, una stagione e le squadre partecipanti.")
public class CompetizioneStagioneDto {
    @JsonProperty("season_key")
    @Schema(description = "Chiave della stagione associata.", example = "2026-2027")
    private String seasonKey;

    @JsonProperty("team_keys")
    @Schema(description = "Chiavi delle squadre partecipanti.")
    private List<String> teamKeys = new ArrayList<>();
}
