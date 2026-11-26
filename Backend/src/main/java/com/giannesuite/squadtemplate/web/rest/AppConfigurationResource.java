package com.giannesuite.squadtemplate.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.giannesuite.squadtemplate.service.CompetizioneService;
import com.giannesuite.squadtemplate.service.GiocatoreService;
import com.giannesuite.squadtemplate.service.SquadraService;
import com.giannesuite.squadtemplate.service.StagioneService;
import com.giannesuite.squadtemplate.service.dto.CompetizioneDto;
import com.giannesuite.squadtemplate.service.dto.GiocatoreDto;
import com.giannesuite.squadtemplate.service.dto.SquadraDto;
import com.giannesuite.squadtemplate.service.dto.StagioneDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** Snapshot di configurazione usato dal client: dati relazionali e opzioni grafiche. */
@Tag(name = "Configurazione", description = "Dati e opzioni usati dal client.")
@RestController
@RequestMapping("/api/config")
public class AppConfigurationResource {
    @Autowired
    private SquadraService squadre;
    @Autowired
    private GiocatoreService giocatori;
    @Autowired
    private StagioneService stagioni;
    @Autowired
    private CompetizioneService competizioni;
    @Autowired
    private AssetResource assets;
    @Value("${app.data-path:../data}")
    private String configuredDataPath;
    private Path dataPath;

    @jakarta.annotation.PostConstruct
    private void initializeDataPath() {
        dataPath = Path.of(configuredDataPath).toAbsolutePath().normalize();
    }

    @GetMapping
    @Operation(summary = "Recupera configurazione client")
    public Map<String, Object> getConfig() throws IOException {
        Map<String, SquadraDto> teamByCode = squadre.selectAll();
        Map<Long, String> teamCodeById = teamByCode.values().stream()
                .filter(team -> team.getId() != null)
                .collect(Collectors.toMap(SquadraDto::getId, SquadraDto::getKey, (a, b) -> a));
        List<Map<String, Object>> teamList = teamByCode.values().stream()
                .map(this::teamConfig).toList();
        List<Map<String, Object>> playerList = giocatori.selectAll().values().stream()
                .map(player -> playerConfig(player, teamCodeById)).toList();
        List<Map<String, Object>> seasonList = stagioni.selectAll().values().stream()
                .map(this::seasonConfig).toList();
        List<Map<String, Object>> competitionList = competizioni.selectAll().values().stream()
                .map(this::competitionConfig).toList();

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("teams", teamList);
        config.put("players", playerList);
        config.put("seasons", seasonList);
        config.put("competitions", competitionList);
        Map<String, Object> assetCatalog = assets.listAssets();
        config.put("emotions", assetCatalog.getOrDefault("emotions", List.of()));
        config.put("backgrounds", assetCatalog.getOrDefault("backgrounds", List.of()));
        config.put("formats", List.of(
                format("9:16", "Story / Reels (9:16)", "1080x1920", "Instagram Story, Reels, TikTok"),
                format("4:3", "Post Orizzontale (4:3)", "1440x1080", "Facebook, Feed Orizzontale"),
                format("1:1", "Post Quadrato (1:1)", "1080x1080", "Instagram Feed Quadrato"),
                format("4:5", "Post Verticale (4:5)", "1080x1350", "Instagram Portrait"),
                format("16:9", "Widescreen (16:9)", "1920x1080", "Banner, X / Twitter")));
        config.put("figurina_formats", List.of(
                Map.of("id", "story", "name", "Storia Instagram (9:16)", "desc", "Card con sfondo in formato Story"),
                Map.of("id", "card", "name", "Figurina pura", "desc", "Formato card 1024x1536"),
                Map.of("id", "post", "name", "Post portrait (4:5)", "desc", "Card per feed 1080x1350")));
        config.put("example_match", loadExampleMatch());
        return config;
    }

    private Map<String, Object> teamConfig(SquadraDto dto) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", dto.getKey()); item.put("id", dto.getId()); item.put("name", dto.getName());
        item.put("extended_name", dto.getExtendedName()); item.put("short_name", dto.getShortName());
        item.put("primary_color", dto.getPrimaryColor()); item.put("secondary_color", dto.getSecondaryColor());
        item.put("tertiary_color", dto.getTertiaryColor()); item.put("notes", dto.getNotes());
        item.put("logo_filename", dto.getLogoFilename());
        item.put("logo_url", dto.getLogoFilename() == null ? null : "/assets/logos/" + dto.getLogoFilename());
        if (!"accv".equalsIgnoreCase(dto.getKey())) item.put("rivalry", dto.getRivalry());
        return item;
    }

    private Map<String, Object> playerConfig(GiocatoreDto dto, Map<Long, String> teamCodeById) {
        Map<String, Object> item = new LinkedHashMap<>();
        String photo = dto.getPhotoFilename();
        item.put("key", dto.getKey()); item.put("id", dto.getId()); item.put("name", dto.getName());
        item.put("display_name", dto.getDisplayName()); item.put("role", dto.getRole()); item.put("number", dto.getNumber());
        item.put("team_id", dto.getTeamId()); item.put("team", teamCodeById.get(dto.getTeamId()));
        item.put("photo_filename", photo); item.put("has_photo", photo != null && !photo.isBlank());
        item.put("photo_url", photo == null || photo.isBlank() ? null : "/assets/players/headshot/transparent/" + photo);
        return item;
    }

    private Map<String, Object> seasonConfig(StagioneDto dto) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", dto.getKey()); item.put("id", dto.getId()); item.put("description", dto.getDescription()); item.put("notes", dto.getNotes());
        return item;
    }

    private Map<String, Object> competitionConfig(CompetizioneDto dto) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", dto.getKey()); item.put("id", dto.getId()); item.put("description", dto.getDescription());
        item.put("notes", dto.getNotes()); item.put("logo_filename", dto.getLogoFilename()); item.put("seasons", dto.getSeasons());
        item.put("logo_url", dto.getLogoFilename() == null ? null : "/assets/logos/" + dto.getLogoFilename());
        return item;
    }

    private Map<String, String> format(String id, String name, String dimensions, String recommended) {
        return Map.of("id", id, "name", name, "dims", dimensions, "recommended", recommended);
    }

    private Object loadExampleMatch() {
        Path example = dataPath.resolve("example_match.json");
        if (!Files.isRegularFile(example)) return Map.of();
        try { return new tools.jackson.databind.ObjectMapper().readValue(example.toFile(), Object.class); }
        catch (RuntimeException ignored) { return Map.of(); }
    }
}
