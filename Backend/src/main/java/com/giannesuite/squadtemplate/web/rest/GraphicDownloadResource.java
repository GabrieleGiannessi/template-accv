package com.giannesuite.squadtemplate.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import tools.jackson.databind.ObjectMapper;
import com.giannesuite.squadtemplate.rendering.FigurinaGraphicRenderer;
import com.giannesuite.squadtemplate.rendering.MatchResultGraphicRenderer;
import com.giannesuite.squadtemplate.rendering.MvpGraphicRenderer;
import com.giannesuite.squadtemplate.rendering.NextMatchGraphicRenderer;
import com.giannesuite.squadtemplate.service.CompetizioneService;
import com.giannesuite.squadtemplate.service.StagioneService;
import com.giannesuite.squadtemplate.service.dto.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Download PNG singoli e pacchetti ZIP tramite gli stessi renderer delle anteprime. */
@Tag(name = "Download grafiche", description = "Esportazione delle grafiche PNG o in archivio.")
@RestController
@RequestMapping("/api")
public class GraphicDownloadResource {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MatchResultGraphicRenderer resultRenderer;
    @Autowired
    private NextMatchGraphicRenderer nextRenderer;
    @Autowired
    private MvpGraphicRenderer mvpRenderer;
    @Autowired
    private FigurinaGraphicRenderer figurinaRenderer;
    @Autowired
    private CompetizioneService competizioni;
    @Autowired
    private StagioneService stagioni;
    @Value("${resource-path:C:/webdata/squadtemplate/}")
    private String configuredResourcePath;
    @Value("${competizioni-loghi:competizioni-loghi}")
    private String competitionLogoDirectory;
    private Path resourcePath;

    @PostConstruct
    private void initializeResourcePath() {
        resourcePath = Path.of(configuredResourcePath);
    }

    @PostMapping("/download")
    @Operation(summary = "Scarica una grafica PNG")
    public ResponseEntity<byte[]> download(@RequestBody Map<String, Object> payload) throws IOException {
        String type = graphicType(payload);
        String format = stringValue(payload.get("format"), type.equals("figurina") ? "card" : type.equals("mvp") ? "4:5" : "9:16");
        byte[] bytes = png(render(type, payload, format));
        String name = filename(type, payload, format);
        return attachment(bytes, MediaType.IMAGE_PNG, name);
    }

    @PostMapping("/download-all")
    @Operation(summary = "Scarica tutte le grafiche in archivio")
    public ResponseEntity<byte[]> downloadAll(@RequestBody Map<String, Object> payload) throws IOException {
        String type = graphicType(payload);
        List<String> formats = switch (type) {
            case "result", "mvp", "next" -> List.of("9:16", "4:3", "16:9", "1:1", "4:5");
            case "figurina" -> List.of("card", "story", "post");
            default -> throw new IllegalArgumentException("Tipo grafica non supportato: " + type);
        };
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
            for (String format : formats) {
                zip.putNextEntry(new ZipEntry(filename(type, payload, format)));
                zip.write(png(render(type, payload, format)));
                zip.closeEntry();
            }
        }
        return attachment(bytes.toByteArray(), MediaType.parseMediaType("application/zip"), "accv_" + type + "_pack.zip");
    }

    private BufferedImage render(String type, Map<String, Object> source, String format) {
        Map<String, Object> payload = new LinkedHashMap<>(source);
        payload.remove("type");
        payload.put("format", format);
        return switch (type) {
            case "result" -> resultRenderer.render(objectMapper.convertValue(enrichResult(payload), MatchResultGraphicRequest.class));
            case "next" -> nextRenderer.render(objectMapper.convertValue(payload, NextMatchGraphicRequest.class));
            case "mvp" -> mvpRenderer.render(objectMapper.convertValue(payload, MvpGraphicRequest.class));
            case "figurina" -> figurinaRenderer.render(objectMapper.convertValue(payload, FigurinaGraphicRequest.class));
            default -> throw new IllegalArgumentException("Tipo grafica non supportato: " + type);
        };
    }

    private Map<String, Object> enrichResult(Map<String, Object> payload) {
        Object competitionKey = payload.get("competition_key");
        if (competitionKey != null) {
            CompetizioneDto competition = competizioni.selectAll().get(competitionKey.toString());
            if (competition != null) {
                payload.putIfAbsent("competition_description", competition.getDescription());
                if (isBlank(payload.get("competition_logo_path")) && competition.getLogoFilename() != null) {
                    payload.put("competition_logo_path", resourcePath.resolve(competitionLogoDirectory)
                            .resolve(competition.getLogoFilename()).toString());
                }
            }
        }
        Object seasonKey = payload.get("season_key");
        if (seasonKey != null) {
            StagioneDto season = stagioni.selectAll().get(seasonKey.toString());
            if (season != null) payload.putIfAbsent("season_description", season.getDescription());
        }
        return payload;
    }

    private byte[] png(BufferedImage image) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (!javax.imageio.ImageIO.write(image, "png", output)) throw new IllegalStateException("Encoder PNG non disponibile");
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile serializzare la grafica", e);
        }
    }

    private ResponseEntity<byte[]> attachment(byte[] body, MediaType contentType, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
        headers.setContentLength(body.length);
        return ResponseEntity.ok().headers(headers).body(body);
    }

    private String graphicType(Map<String, Object> payload) {
        String type = stringValue(payload.get("type"), "result").toLowerCase(Locale.ROOT);
        if (!List.of("result", "next", "mvp", "figurina").contains(type)) {
            throw new IllegalArgumentException("Tipo grafica non supportato: " + type);
        }
        return type;
    }

    private String filename(String type, Map<String, Object> payload, String format) {
        String suffix = format.toLowerCase(Locale.ROOT).replace(':', '_').replaceAll("[^a-z0-9_-]", "_");
        String stem = switch (type) {
            case "result" -> "match_result";
            case "next" -> "next_match";
            case "mvp" -> "mvp";
            default -> "figurina_" + safeFilename(stringValue(payload.get("player_name"), "figurina"));
        };
        return stem + "_" + suffix + ".png";
    }

    private String safeFilename(String input) {
        String safe = input.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        return safe.isBlank() ? "figurina" : safe;
    }

    private String stringValue(Object value, String fallback) { return value == null || value.toString().isBlank() ? fallback : value.toString(); }
    private boolean isBlank(Object value) { return value == null || value.toString().isBlank(); }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> invalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("status", "error", "message", exception.getMessage()));
    }
}
