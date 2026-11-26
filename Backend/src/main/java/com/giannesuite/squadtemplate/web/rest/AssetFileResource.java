package com.giannesuite.squadtemplate.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

/** Serve gli asset con URL stabili, impedendo l'uscita dalla cartella configurata. */
@Tag(name = "File asset", description = "Accesso ai file immagine pubblicati dal backend.")
@RestController
@RequestMapping("/assets")
public class AssetFileResource {
    @Value("${graphics.assets-path:../assets}")
    private String assetsDirectory;
    private Path assetsPath;

    @jakarta.annotation.PostConstruct
    private void initializeAssetsPath() {
        assetsPath = Path.of(assetsDirectory).toAbsolutePath().normalize();
    }

    @GetMapping("/{*assetPath}")
    @Operation(summary = "Recupera un file asset")
    public ResponseEntity<FileSystemResource> getAsset(@PathVariable String assetPath) {
        Path resolved = assetsPath.resolve(assetPath.replaceFirst("^/", "")).normalize();
        if (!resolved.startsWith(assetsPath) || !Files.isRegularFile(resolved)) return ResponseEntity.notFound().build();
        FileSystemResource resource = new FileSystemResource(resolved);
        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(resource).orElse(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM))
                .body(resource);
    }
}
