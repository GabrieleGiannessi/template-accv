package com.giannesuite.squadtemplate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class SquadTemplateApplication implements ApplicationRunner {
    private final Path resourcePath;

    public SquadTemplateApplication(@Value("${resource-path}") String resourcePath) {
        this.resourcePath = Path.of(resourcePath);
    }

    public static void main(String[] args) {
        SpringApplication.run(SquadTemplateApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Files.createDirectories(resourcePath);
        } catch (IOException exception) {
            throw new UncheckedIOException("Impossibile creare la cartella risorse: " + resourcePath, exception);
        }
    }
}
