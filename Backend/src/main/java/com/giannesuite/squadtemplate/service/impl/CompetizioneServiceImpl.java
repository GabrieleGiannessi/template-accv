package com.giannesuite.squadtemplate.service.impl;

import com.giannesuite.squadtemplate.dao.CompetizioneDao;
import com.giannesuite.squadtemplate.dao.model.Competizione;
import com.giannesuite.squadtemplate.service.CompetizioneService;
import com.giannesuite.squadtemplate.service.dto.CompetizioneDto;
import com.giannesuite.squadtemplate.service.dto.CompetizioneStagioneDto;
import com.giannesuite.squadtemplate.service.mapper.CompetizioneMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompetizioneServiceImpl implements CompetizioneService {
    @Autowired
    private CompetizioneDao competizioneDao;
    @Autowired
    private CompetizioneMapper competizioneMapper;
    @Value("${resource-path}")
    private String resourcePath;
    @Value("${competizioni-loghi}")
    private String directoryRelativaLoghi;
    private Path directoryLoghi;

    private CompetizioneDto preparaCompetizione(CompetizioneDto dto) {
        String codice = normalizzaCodice(dto.getKey());
        if (codice.isBlank()) {
            throw new IllegalArgumentException("Il codice competizione è obbligatorio");
        }
        dto.setKey(codice);
        dto.setDescription(dto.getDescription().trim());
        dto.setNotes(dto.getNotes() == null ? "" : dto.getNotes().trim());
        dto.setLogoFilename(normalizzaNomeLogo(dto.getLogoFilename()));
        return dto;
    }

    private String normalizzaCodice(String value) {
        return value == null ? "" : value.trim().toLowerCase().replace(' ', '-');
    }

    private String normalizzaNomeLogo(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String nomeFile = value.trim();
        if (nomeFile.contains("/") || nomeFile.contains("\\") || nomeFile.equals(".") || nomeFile.equals("..")) {
            throw new IllegalArgumentException("Il logo deve essere indicato con il solo nome del file");
        }
        return nomeFile;
    }

    private CompetizioneDto aggiungiAssociazioni(CompetizioneDto dto) {
        if (dto != null) {
            dto.setSeasons(competizioneDao.selectAssociazioni(dto.getId()));
        }
        return dto;
    }

    private Path creaDirectoryLoghi(Path resourcePath, String directoryRelativa) {
        Path resourcePathNormalizzato = resourcePath.toAbsolutePath().normalize();
        Path directory = resourcePathNormalizzato.resolve(directoryRelativa).normalize();
        if (!directory.startsWith(resourcePathNormalizzato)) {
            throw new IllegalArgumentException("La directory dei loghi deve trovarsi dentro resource-path");
        }
        try {
            Files.createDirectories(directory);
        } catch (IOException exception) {
            throw new UncheckedIOException("Impossibile creare la cartella dei loghi: " + directory, exception);
        }
        return directory;
    }

    @PostConstruct
    private void inizializzaDirectoryLoghi() {
        directoryLoghi = creaDirectoryLoghi(Path.of(resourcePath), directoryRelativaLoghi);
    }

    @Override
    @Transactional(readOnly = true)
    public CompetizioneDto getCompetizioneById(long id) {
        Competizione competizione = competizioneDao.getCompetizioneById(id);
        return competizione == null ? null : aggiungiAssociazioni(competizioneMapper.fromModel(competizione));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, CompetizioneDto> selectAll() {
        Map<String, CompetizioneDto> competizioni = new LinkedHashMap<>();
        for (Competizione competizione : competizioneDao.selectAll()) {
            competizioni.put(competizione.getCodice(), aggiungiAssociazioni(competizioneMapper.fromModel(competizione)));
        }
        return competizioni;
    }

    @Override
    @Transactional
    public CompetizioneDto addCompetizione(CompetizioneDto dto) {
        Competizione competizione = competizioneDao.addCompetizione(preparaCompetizione(dto));
        if (dto.getSeasons() != null) {
            competizioneDao.replaceAssociazioni(competizione.getId(), dto.getSeasons());
        }
        return aggiungiAssociazioni(competizioneMapper.fromModel(competizione));
    }

    @Override
    @Transactional
    public CompetizioneDto updateCompetizione(long id, CompetizioneDto dto) {
        dto.setId(id);
        Competizione competizioneAggiornata = competizioneDao.updateCompetizione(preparaCompetizione(dto));
        if (competizioneAggiornata == null) {
            return null;
        }
        if (dto.getSeasons() != null) {
            competizioneDao.replaceAssociazioni(id, dto.getSeasons());
        }
        return aggiungiAssociazioni(competizioneMapper.fromModel(competizioneAggiornata));
    }

    @Override
    @Transactional
    public boolean deleteCompetizione(long id) {
        return competizioneDao.deleteCompetizione(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompetizioneStagioneDto> getAssociazioni(long idCompetizione) {
        if (competizioneDao.getCompetizioneById(idCompetizione) == null) {
            return null;
        }
        return competizioneDao.selectAssociazioni(idCompetizione);
    }

    @Override
    @Transactional
    public List<CompetizioneStagioneDto> replaceAssociazioni(
            long idCompetizione,
            List<CompetizioneStagioneDto> associazioni) {
        if (competizioneDao.getCompetizioneById(idCompetizione) == null) {
            return null;
        }
        competizioneDao.replaceAssociazioni(idCompetizione, associazioni);
        return competizioneDao.selectAssociazioni(idCompetizione);
    }
}
