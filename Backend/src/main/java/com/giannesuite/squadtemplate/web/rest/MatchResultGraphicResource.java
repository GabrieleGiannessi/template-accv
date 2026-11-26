package com.giannesuite.squadtemplate.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.giannesuite.squadtemplate.utils.GraphicPreviewSupport;
import com.giannesuite.squadtemplate.rendering.MatchResultGraphicRenderer;
import com.giannesuite.squadtemplate.service.dto.MatchResultGraphicRequest;
import com.giannesuite.squadtemplate.service.dto.RenderedGraphicDto;
import com.giannesuite.squadtemplate.service.CompetizioneService;
import com.giannesuite.squadtemplate.service.StagioneService;
import com.giannesuite.squadtemplate.service.dto.CompetizioneDto;
import com.giannesuite.squadtemplate.service.dto.StagioneDto;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@Tag(name = "Anteprima risultato", description = "Generazione della grafica del risultato.")
@RestController
@RequestMapping("/api/preview")
public class MatchResultGraphicResource {
    @Autowired
    private MatchResultGraphicRenderer renderer;
    @Autowired
    private CompetizioneService competizioni;
    @Autowired
    private StagioneService stagioni;
    @Value("${resource-path:C:/webdata/squadtemplate/}")
    private String configuredResourcePath;
    @Value("${competizioni-loghi:competizioni-loghi}")
    private String logoDirectory;
    private Path resourcePath;

    @PostConstruct
    private void initializeResourcePath() {
        resourcePath = Path.of(configuredResourcePath);
    }

    @PostMapping("/result")
    @Operation(summary = "Genera anteprima risultato partita")
    public RenderedGraphicDto render(@RequestBody(required = false) MatchResultGraphicRequest request) throws IOException {
        if (request == null) request = new MatchResultGraphicRequest(null,null,null,null,null,null,null,null,null,null,
                null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);
        request = enrichFromCatalog(request);
        String format = request.format() == null || request.format().isBlank() ? "9:16" : request.format();
        return GraphicPreviewSupport.asPng(renderer.render(request), format, renderer.resolveBackgroundPath(request));
    }

    private MatchResultGraphicRequest enrichFromCatalog(MatchResultGraphicRequest request) {
        String competitionDescription = request.competition_description();
        String seasonDescription = request.season_description();
        String logoPath = request.competition_logo_path();
        if (request.competition_key() != null && !request.competition_key().isBlank()) {
            CompetizioneDto competition = competizioni.selectAll().get(request.competition_key());
            if (competition != null) {
                if (competitionDescription == null || competitionDescription.isBlank()) competitionDescription = competition.getDescription();
                if ((logoPath == null || logoPath.isBlank()) && competition.getLogoFilename() != null) {
                    logoPath = resourcePath.resolve(logoDirectory).resolve(competition.getLogoFilename()).toString();
                }
            }
        }
        if (request.season_key() != null && !request.season_key().isBlank()) {
            StagioneDto season = stagioni.selectAll().get(request.season_key());
            if (season != null && (seasonDescription == null || seasonDescription.isBlank())) seasonDescription = season.getDescription();
        }
        return new MatchResultGraphicRequest(request.format(), request.bg_path(), request.emotion(), request.contrast_factor(),
                request.remove_contrast(), request.bg_zoom(), request.bg_x(), request.bg_y(), request.graphic_variant(),
                request.mix_team_colors(), request.home_score(), request.away_score(), request.home_team(), request.away_team(),
                request.home_scorers(), request.away_scorers(), request.home_yellow_cards(), request.away_yellow_cards(),
                request.home_red_cards(), request.away_red_cards(), request.include_competition_info(), competitionDescription,
                seasonDescription, logoPath, request.competition_key(), request.season_key());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> invalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", exception.getMessage()));
    }
}
