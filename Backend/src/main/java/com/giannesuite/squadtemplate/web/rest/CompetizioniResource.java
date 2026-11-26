package com.giannesuite.squadtemplate.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.giannesuite.squadtemplate.service.CompetizioneService;
import com.giannesuite.squadtemplate.service.dto.CompetizioneDto;
import com.giannesuite.squadtemplate.service.dto.CompetizioneStagioneDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

@Validated
@Tag(name = "Competizioni", description = "Gestione delle competizioni e associazioni con le stagioni.")
@RestController
@RequestMapping("/api/competitions")
public class CompetizioniResource {
    @Autowired
    private CompetizioneService competizioneService;

    @GetMapping
    @Operation(summary = "Elenca competizioni")
    public Map<String, CompetizioneDto> selectAll() {
        return competizioneService.selectAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recupera competizione tramite ID")
    public ResponseEntity<CompetizioneDto> getCompetizioneById(@PathVariable @Positive long id) {
        CompetizioneDto competizione = competizioneService.getCompetizioneById(id);
        return competizione == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(competizione);
    }

    @PostMapping
    @Operation(summary = "Crea competizione")
    public ResponseEntity<Map<String, Object>> addCompetizione(@Valid @RequestBody CompetizioneDto dto) {
        CompetizioneDto competizione = competizioneService.addCompetizione(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", "ok",
                "message", "Competizione aggiunta con successo",
                "competition", competizione,
                "competitions", competizioneService.selectAll()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aggiorna competizione tramite ID")
    public ResponseEntity<Map<String, Object>> updateCompetizione(
            @PathVariable @Positive long id,
            @Valid @RequestBody CompetizioneDto dto) {
        CompetizioneDto competizione = competizioneService.updateCompetizione(id, dto);
        if (competizione == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "Competizione non trovata"));
        }
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Competizione aggiornata con successo",
                "competition", competizione,
                "competitions", competizioneService.selectAll()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina competizione tramite ID")
    public ResponseEntity<Map<String, Object>> deleteCompetizione(@PathVariable @Positive long id) {
        if (!competizioneService.deleteCompetizione(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "Competizione non trovata"));
        }
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Competizione " + id + " eliminata",
                "competitions", competizioneService.selectAll()));
    }

    @GetMapping("/{id}/associations")
    @Operation(summary = "Elenca le associazioni della competizione")
    public ResponseEntity<List<CompetizioneStagioneDto>> getAssociazioni(@PathVariable @Positive long id) {
        List<CompetizioneStagioneDto> associazioni = competizioneService.getAssociazioni(id);
        return associazioni == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(associazioni);
    }

    @PutMapping("/{id}/associations")
    @Operation(summary = "Sostituisce le associazioni della competizione")
    public ResponseEntity<List<CompetizioneStagioneDto>> replaceAssociazioni(
            @PathVariable @Positive long id,
            @RequestBody List<CompetizioneStagioneDto> associazioni) {
        List<CompetizioneStagioneDto> aggiornate = competizioneService.replaceAssociazioni(id, associazioni);
        return aggiornate == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(aggiornate);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> gestisciVincoliDatabase(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", "error",
                "message", "Esiste già una competizione con lo stesso codice"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> gestisciRichiestaNonValida(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", exception.getMessage()));
    }
}
