package com.giannesuite.squadtemplate.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Dati anagrafici e sportivi di un giocatore.")
public class GiocatoreDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty(value = "key", access = Access.WRITE_ONLY)
    @Schema(description = "Chiave univoca del giocatore, richiesta in scrittura.", example = "mario-rossi")
    @NotBlank
    @Size(max = 80)
    private String key;

    @JsonProperty("name")
    @Schema(description = "Nome completo del giocatore.", example = "Mario Rossi")
    @NotBlank
    @Size(max = 120)
    private String name;

    @JsonProperty("display_name")
    @Size(max = 120)
    private String displayName;

    @JsonProperty("role")
    @Schema(description = "Ruolo del giocatore.", example = "Attaccante")
    @NotBlank
    @Size(max = 40)
    private String role;

    @JsonProperty("number")
    @Size(max = 10)
    private String number;

    @JsonProperty("team_id")
    @Schema(description = "ID della squadra di appartenenza.", example = "1")
    @NotNull
    @Positive
    private Long teamId;

    @JsonProperty("photo_filename")
    @Size(max = 255)
    private String photoFilename;
}
