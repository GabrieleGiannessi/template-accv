package com.giannesuite.squadtemplate.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.giannesuite.squadtemplate.service.dto.SquadraDto;
import com.giannesuite.squadtemplate.service.SquadraService;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Validated
@Tag(name = "Squadre", description = "Gestione delle squadre.")
@RestController
@RequestMapping("/api/teams")
public class SquadreResource {
    @Autowired
    private SquadraService squadreService;

    @GetMapping
    @Operation(summary = "Elenca squadre")
    public Map<String, SquadraDto> trovaTutte() {
        return squadreService.selectAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recupera squadra tramite ID")
    public ResponseEntity<SquadraDto> getSquadraById(@PathVariable @Positive long id) {
        SquadraDto squadra = squadreService.getSquadraById(id);
        return squadra == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(squadra);
    }

    @PostMapping
    @Operation(summary = "Crea squadra")
    public Map<String, Object> salva(@Valid @RequestBody SquadraDto dto) {
        squadreService.addSquadra(dto);
        return Map.of(
                "status", "ok",
                "message", "Squadra aggiunta con successo",
                "teams", squadreService.selectAll()
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aggiorna squadra tramite ID")
    public ResponseEntity<Map<String, Object>> updateSquadra(
            @PathVariable @Positive long id,
            @Valid @RequestBody SquadraDto dto) {
        SquadraDto squadra = squadreService.updateSquadra(id, dto);
        if (squadra == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "Squadra non trovata"));
        }
        return ResponseEntity.ok(Map.<String, Object>of(
                        "status", "ok",
                        "message", "Squadra aggiornata con successo",
                        "team", squadra,
                        "teams", squadreService.selectAll()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina squadra tramite ID")
    public ResponseEntity<Map<String, Object>> deleteSquadra(@PathVariable @Positive long id) {
        if (!squadreService.deleteSquadra(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "Squadra non trovata"));
        }
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Squadra " + id + " eliminata",
                "teams", squadreService.selectAll()));
    }

    /** Endpoint legacy: mantiene la cancellazione tramite codice usata dal client esistente. */
    @PostMapping("/delete")
    @Operation(summary = "Elimina una squadra tramite chiave legacy")
    public ResponseEntity<Map<String, Object>> elimina(@RequestBody EliminazioneSquadraRequest request) {
        if (request == null || request.key() == null || request.key().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Codice squadra obbligatorio"));
        }
        if (!squadreService.deleteSquadraByCodice(request.key())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "Squadra non trovata"));
        }
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Squadra " + request.key() + " eliminata",
                "teams", squadreService.selectAll()
        ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> gestisciVincoliDatabase(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("status", "error", "message", "La squadra è ancora utilizzata da altri dati"));
    }

    @Schema(description = "Richiesta legacy per eliminare una squadra tramite chiave.")
    public record EliminazioneSquadraRequest(
            @Schema(description = "Chiave della squadra da eliminare.", example = "accv") @NotBlank String key) {
    }
}
