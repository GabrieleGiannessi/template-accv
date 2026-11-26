package com.giannesuite.squadtemplate.service.impl;

import com.giannesuite.squadtemplate.dao.StagioneDao;
import com.giannesuite.squadtemplate.dao.model.Stagione;
import com.giannesuite.squadtemplate.service.StagioneService;
import com.giannesuite.squadtemplate.service.dto.StagioneDto;
import com.giannesuite.squadtemplate.service.mapper.StagioneMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class StagioneServiceImpl implements StagioneService {
    @Autowired
    private StagioneDao stagioneDao;
    @Autowired
    private StagioneMapper stagioneMapper;

    private StagioneDto preparaStagione(StagioneDto dto) {
        String codice = normalizzaCodice(dto.getKey());
        if (codice.isBlank()) {
            throw new IllegalArgumentException("Il codice stagione è obbligatorio");
        }
        dto.setKey(codice);
        dto.setDescription(dto.getDescription().trim());
        dto.setNotes(dto.getNotes() == null ? "" : dto.getNotes().trim());
        return dto;
    }

    private String normalizzaCodice(String value) {
        return value == null ? "" : value.trim().toLowerCase().replace(' ', '-');
    }

    @Override
    @Transactional(readOnly = true)
    public StagioneDto getStagioneById(long id) {
        Stagione stagione = stagioneDao.getStagioneById(id);
        return stagione == null ? null : stagioneMapper.fromModel(stagione);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, StagioneDto> selectAll() {
        Map<String, StagioneDto> stagioni = new LinkedHashMap<>();
        for (Stagione stagione : stagioneDao.selectAll()) {
            stagioni.put(stagione.getCodice(), stagioneMapper.fromModel(stagione));
        }
        return stagioni;
    }

    @Override
    @Transactional
    public StagioneDto addStagione(StagioneDto dto) {
        return stagioneMapper.fromModel(stagioneDao.addStagione(preparaStagione(dto)));
    }

    @Override
    @Transactional
    public StagioneDto updateStagione(long id, StagioneDto dto) {
        dto.setId(id);
        Stagione stagioneAggiornata = stagioneDao.updateStagione(preparaStagione(dto));
        return stagioneAggiornata == null ? null : stagioneMapper.fromModel(stagioneAggiornata);
    }

    @Override
    @Transactional
    public boolean deleteStagione(long id) {
        return stagioneDao.deleteStagione(id);
    }
}
