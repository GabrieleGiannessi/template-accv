package com.giannesuite.squadtemplate.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.giannesuite.squadtemplate.service.StagioneService;
import com.giannesuite.squadtemplate.service.dto.StagioneDto;
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

@Validated
@Tag(name = "Stagioni", description = "Gestione delle stagioni.")
@RestController
@RequestMapping("/api/seasons")
public class StagioniResource {
    @Autowired
    private StagioneService stagioneService;

    @GetMapping
    @Operation(summary = "Elenca stagioni")
    public Map<String, StagioneDto> selectAll() {
        return stagioneService.selectAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recupera stagione tramite ID")
    public ResponseEntity<StagioneDto> getStagioneById(@PathVariable @Positive long id) {
        StagioneDto stagione = stagioneService.getStagioneById(id);
        return stagione == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(stagione);
    }

    @PostMapping
    @Operation(summary = "Crea stagione")
    public ResponseEntity<Map<String, Object>> addStagione(@Valid @RequestBody StagioneDto dto) {
        StagioneDto stagione = stagioneService.addStagione(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", "ok",
                "message", "Stagione aggiunta con successo",
                "season", stagione,
                "seasons", stagioneService.selectAll()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aggiorna stagione tramite ID")
    public ResponseEntity<Map<String, Object>> updateStagione(
            @PathVariable @Positive long id,
            @Valid @RequestBody StagioneDto dto) {
        StagioneDto stagione = stagioneService.updateStagione(id, dto);
        if (stagione == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "Stagione non trovata"));
        }
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Stagione aggiornata con successo",
                "season", stagione,
                "seasons", stagioneService.selectAll()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina stagione tramite ID")
    public ResponseEntity<Map<String, Object>> deleteStagione(@PathVariable @Positive long id) {
        if (!stagioneService.deleteStagione(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "Stagione non trovata"));
        }
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Stagione " + id + " eliminata",
                "seasons", stagioneService.selectAll()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> gestisciVincoliDatabase(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", "error",
                "message", "La stagione è già esistente o ancora utilizzata da altre associazioni"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> gestisciRichiestaNonValida(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", exception.getMessage()));
    }
}
