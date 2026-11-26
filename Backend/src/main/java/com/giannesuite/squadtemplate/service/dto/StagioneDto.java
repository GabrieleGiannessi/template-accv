package com.giannesuite.squadtemplate.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Dati identificativi e descrittivi di una stagione.")
public class StagioneDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty(value = "key", access = Access.WRITE_ONLY)
    @Schema(description = "Chiave univoca della stagione, richiesta in scrittura.", example = "2026-2027")
    @NotBlank
    @Size(max = 80)
    private String key;

    @JsonProperty("description")
    @Schema(description = "Descrizione visualizzata della stagione.", example = "Stagione 2026/2027")
    @NotBlank
    @Size(max = 120)
    private String description;

    @JsonProperty("notes")
    private String notes;
}
