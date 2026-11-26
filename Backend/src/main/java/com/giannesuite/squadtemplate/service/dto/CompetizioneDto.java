package com.giannesuite.squadtemplate.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Dati identificativi e descrittivi di una competizione.")
public class CompetizioneDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty(value = "key", access = Access.WRITE_ONLY)
    @Schema(description = "Chiave univoca della competizione, richiesta in creazione e modifica.", example = "coppa-italia")
    @NotBlank
    @Size(max = 80)
    private String key;

    @JsonProperty("description")
    @Schema(description = "Nome visualizzato della competizione.", example = "Coppa Italia")
    @NotBlank
    @Size(max = 160)
    private String description;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("logo_filename")
    @Schema(description = "Nome file del logo della competizione.", example = "competition_coppa_italia.png")
    @Size(max = 255)
    private String logoFilename;

    // Campo mantenuto per la compatibilità con il payload legacy delle competizioni.
    @JsonProperty("seasons")
    private List<CompetizioneStagioneDto> seasons;
}
