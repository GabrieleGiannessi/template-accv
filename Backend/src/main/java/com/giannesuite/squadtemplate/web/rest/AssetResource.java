package com.giannesuite.squadtemplate.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.giannesuite.squadtemplate.utils.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/** Catalogo e caricamento controllato degli asset grafici. */
@Tag(name = "Asset", description = "Catalogo e caricamento degli asset grafici.")
@RestController
@RequestMapping("/api")
public class AssetResource {
    @Value("${graphics.assets-path:../assets}")
    private String assetsDirectory;
    @Value("${resource-path:C:/webdata/squadtemplate/}")
    private String resourceDirectory;
    @Value("${competizioni-loghi:competizioni-loghi}")
    private String competitionLogosDirectory;
    private Path assetsPath;
    private Path competitionLogosPath;

    @jakarta.annotation.PostConstruct
    private void initializeAssetPaths() {
        this.assetsPath = Path.of(assetsDirectory).toAbsolutePath().normalize();
        this.competitionLogosPath = Path.of(resourceDirectory).resolve(competitionLogosDirectory).toAbsolutePath().normalize();
    }

    @GetMapping("/assets")
    @Operation(summary = "Elenca gli asset grafici disponibili")
    public Map<String, Object> listAssets() throws IOException {
        Path backgroundsRoot = assetsPath.resolve("backgrounds");
        List<Map<String, String>> backgrounds = new ArrayList<>();
        List<String> emotions = new ArrayList<>();
        if (Files.isDirectory(backgroundsRoot)) {
            Path themes = backgroundsRoot.resolve("temi");
            if (Files.isDirectory(themes)) {
                try (Stream<Path> dirs = Files.list(themes)) {
                    dirs.filter(Files::isDirectory).map(p -> p.getFileName().toString()).sorted(String.CASE_INSENSITIVE_ORDER).forEach(emotions::add);
                }
            } else {
                try (Stream<Path> dirs = Files.list(backgroundsRoot)) {
                    dirs.filter(Files::isDirectory).map(p -> p.getFileName().toString())
                            .filter(n -> n.toLowerCase(Locale.ROOT).startsWith("tema "))
                            .map(n -> n.substring(5)).sorted(String.CASE_INSENSITIVE_ORDER).forEach(emotions::add);
                }
            }
            if (Files.isDirectory(backgroundsRoot.resolve("foto_di_gruppo")) || Files.isDirectory(backgroundsRoot.resolve("foto squadra"))) {
                emotions.add("foto squadra");
            }
            try (Stream<Path> paths = Files.walk(backgroundsRoot)) {
                paths.filter(Files::isRegularFile).filter(this::isImage).sorted(Comparator.comparing(Path::toString))
                        .forEach(path -> backgrounds.add(backgroundInfo(backgroundsRoot, path)));
            }
        }
        return Map.of("emotions", emotions, "backgrounds", backgrounds);
    }

    @PostMapping("/upload")
    @Operation(summary = "Carica un asset immagine")
    public Map<String, Object> upload(@RequestBody UploadRequest request) throws IOException {
        if (request == null || request.data() == null || request.data().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Il contenuto del file è obbligatorio");
        }
        String type = request.upload_type() == null ? "background" : request.upload_type().toLowerCase(Locale.ROOT);
        Path category = switch (type) {
            case "background" -> assetsPath.resolve("backgrounds");
            case "player" -> assetsPath.resolve("players/headshot/transparent");
            case "logo" -> assetsPath.resolve("logos");
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo di asset non supportato");
        };
        String filename = safeFilename(request.filename());
        String extension = extension(filename);
        if (!Constants.SUPPORTED_IMAGE_EXTENSIONS.contains(extension)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato immagine non supportato");

        String encoded = request.data().trim();
        int comma = encoded.indexOf(',');
        if (encoded.startsWith("data:")) {
            if (comma < 0 || !encoded.substring(0, comma).toLowerCase(Locale.ROOT).matches("data:image/(png|jpeg|webp);base64")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data URI immagine non valido");
            }
            encoded = encoded.substring(comma + 1);
        }
        byte[] bytes;
        try { bytes = Base64.getDecoder().decode(encoded); }
        catch (IllegalArgumentException exception) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contenuto Base64 non valido"); }
        if (bytes.length == 0 || bytes.length > Constants.MAX_UPLOAD_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "L'immagine deve avere dimensione compresa tra 1 byte e 20 MB");
        }
        if (ImageIO.read(new ByteArrayInputStream(bytes)) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Il contenuto caricato non è un'immagine valida o leggibile");
        }

        Files.createDirectories(category);
        Path target = category.resolve(filename).normalize();
        if (!target.startsWith(category.normalize())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome file non valido");
        Files.write(target, bytes);

        // Le grafiche di competizione hanno una directory persistente separata configurabile.
        if (type.equals("logo") && filename.toLowerCase(Locale.ROOT).startsWith("competition_")) {
            Files.createDirectories(competitionLogosPath);
            Files.copy(target, competitionLogosPath.resolve(filename), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("filename", filename);
        response.put("path", target.toString());
        response.put("url", "/assets/" + assetsPath.relativize(target).toString().replace('\\', '/'));
        return response;
    }

    private Map<String, String> backgroundInfo(Path root, Path image) {
        Path relative = root.relativize(image);
        Path parent = relative.getParent();
        String category = "Altro";
        if (parent != null) {
            String folder = parent.getFileName().toString();
            if (parent.getParent() != null && parent.getParent().getFileName().toString().equals("temi")) category = folder;
            else if (folder.toLowerCase(Locale.ROOT).startsWith("tema ")) category = folder.substring(5);
            else if (Set.of("foto_di_gruppo", "foto squadra").contains(folder.toLowerCase(Locale.ROOT))) category = "Foto Squadra";
            else if (parent.getNameCount() == 1 || folder.equalsIgnoreCase("standard")) category = "Predefinito";
        }
        Map<String, String> item = new LinkedHashMap<>();
        item.put("filename", image.getFileName().toString());
        item.put("relative_path", relative.toString().replace('\\', '/'));
        item.put("full_path", image.toString());
        item.put("url", "/assets/backgrounds/" + relative.toString().replace('\\', '/'));
        item.put("category", category);
        return item;
    }

    private boolean isImage(Path path) { return Constants.SUPPORTED_IMAGE_EXTENSIONS.contains(extension(path.getFileName().toString())); }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String safeFilename(String filename) {
        if (filename == null || filename.isBlank()) return "upload.png";
        String normalized = filename.replace('\\', '/');
        if (!Path.of(normalized).getFileName().toString().equals(normalized) || normalized.equals(".") || normalized.equals("..")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Il nome file non può contenere cartelle");
        }
        return normalized;
    }

    @Schema(description = "Dati del file immagine da caricare in formato Base64.")
    public record UploadRequest(
            @Schema(description = "Destinazione: background, player o logo.", example = "background") String upload_type,
            @Schema(description = "Nome file con estensione immagine.", example = "sfondo.jpg") String filename,
            @Schema(description = "Contenuto Base64 oppure data URI dell’immagine.") String data) { }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> invalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("status", "error", "message", exception.getMessage()));
    }
}
