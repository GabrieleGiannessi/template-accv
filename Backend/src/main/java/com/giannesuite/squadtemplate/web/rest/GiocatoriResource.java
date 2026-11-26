package com.giannesuite.squadtemplate.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.giannesuite.squadtemplate.service.GiocatoreService;
import com.giannesuite.squadtemplate.service.dto.GiocatoreDto;
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
@Tag(name = "Giocatori", description = "Gestione dei giocatori.")
@RestController
@RequestMapping("/api/players")
public class GiocatoriResource {
    @Autowired
    private GiocatoreService giocatoreService;

    @GetMapping
    @Operation(summary = "Elenca giocatori")
    public Map<String, GiocatoreDto> selectAll() {
        return giocatoreService.selectAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recupera giocatore tramite ID")
    public ResponseEntity<GiocatoreDto> getGiocatoreById(@PathVariable @Positive long id) {
        GiocatoreDto giocatore = giocatoreService.getGiocatoreById(id);
        return giocatore == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(giocatore);
    }

    @PostMapping
    @Operation(summary = "Crea giocatore")
    public ResponseEntity<Map<String, Object>> addGiocatore(@Valid @RequestBody GiocatoreDto dto) {
        GiocatoreDto giocatore = giocatoreService.addGiocatore(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", "ok",
                "message", "Giocatore aggiunto con successo",
                "player", giocatore,
                "players", giocatoreService.selectAll()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aggiorna giocatore tramite ID")
    public ResponseEntity<Map<String, Object>> updateGiocatore(
            @PathVariable @Positive long id,
            @Valid @RequestBody GiocatoreDto dto) {
        GiocatoreDto giocatore = giocatoreService.updateGiocatore(id, dto);
        if (giocatore == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "Giocatore non trovato"));
        }
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Giocatore aggiornato con successo",
                "player", giocatore,
                "players", giocatoreService.selectAll()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina giocatore tramite ID")
    public ResponseEntity<Map<String, Object>> deleteGiocatore(@PathVariable @Positive long id) {
        if (!giocatoreService.deleteGiocatore(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "Giocatore non trovato"));
        }
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Giocatore " + id + " eliminato",
                "players", giocatoreService.selectAll()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> gestisciVincoliDatabase(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", "error",
                "message", "Il codice giocatore esiste già oppure la squadra indicata non è valida"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> gestisciRichiestaNonValida(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", exception.getMessage()));
    }
}
