package com.giannesuite.squadtemplate.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Dati identificativi, grafici e descrittivi di una squadra.")
public class SquadraDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty(value = "key", access = Access.WRITE_ONLY)
    @Schema(description = "Chiave univoca della squadra, richiesta in scrittura.", example = "accv")
    @NotBlank
    private String key;

    @JsonProperty("name")
    @Schema(description = "Nome della squadra.", example = "A.C.C.V.")
    @NotBlank
    private String name;

    @JsonProperty("extended_name")
    private String extendedName = "";

    @JsonProperty("short_name")
    private String shortName;

    @JsonProperty("primary_color")
    @Schema(description = "Colore principale RGB, con valori compresi fra 0 e 255.", example = "[16, 185, 129]")
    @Size(min = 3, max = 3)
    private List<@NotNull @Min(0) @Max(255) Integer> primaryColor = List.of(16, 185, 129);

    @JsonProperty("secondary_color")
    @Schema(description = "Colore secondario RGB, con valori compresi fra 0 e 255.", example = "[239, 68, 68]")
    @Size(min = 3, max = 3)
    private List<@NotNull @Min(0) @Max(255) Integer> secondaryColor = List.of(239, 68, 68);

    @JsonProperty("tertiary_color")
    @Size(min = 3, max = 3)
    private List<@NotNull @Min(0) @Max(255) Integer> tertiaryColor = List.of(59, 130, 246);

    @JsonProperty("notes")
    private String notes = "";

    @JsonProperty("logo_filename")
    private String logoFilename;

    @JsonProperty("rivalry")
    @Min(1)
    @Max(5)
    private Integer rivalry;
}
