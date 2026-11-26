package com.giannesuite.squadtemplate.service.impl;

import com.giannesuite.squadtemplate.dao.GiocatoreDao;
import com.giannesuite.squadtemplate.dao.SquadraDao;
import com.giannesuite.squadtemplate.dao.model.Giocatore;
import com.giannesuite.squadtemplate.service.GiocatoreService;
import com.giannesuite.squadtemplate.service.dto.GiocatoreDto;
import com.giannesuite.squadtemplate.service.mapper.GiocatoreMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class GiocatoreServiceImpl implements GiocatoreService {
    @Autowired
    private GiocatoreDao giocatoreDao;
    @Autowired
    private SquadraDao squadraDao;
    @Autowired
    private GiocatoreMapper giocatoreMapper;

    private GiocatoreDto preparaGiocatore(GiocatoreDto dto) {
        if (dto.getTeamId() == null) {
            throw new IllegalArgumentException("L'identificativo della squadra è obbligatorio");
        }
        dto.setKey(normalizzaCodice(dto.getKey()));
        dto.setName(dto.getName().trim());
        dto.setRole(dto.getRole().trim());
        dto.setDisplayName(normalizzaOpzionale(dto.getDisplayName()));
        dto.setNumber(normalizzaOpzionale(dto.getNumber()));
        dto.setPhotoFilename(normalizzaOpzionale(dto.getPhotoFilename()));

        if (dto.getKey().isBlank()) {
            throw new IllegalArgumentException("Il codice giocatore è obbligatorio");
        }
        if (squadraDao.getSquadraById(dto.getTeamId()) == null) {
            throw new IllegalArgumentException("La squadra indicata non esiste");
        }
        return dto;
    }

    private String normalizzaCodice(String value) {
        return value == null ? "" : value.trim().toLowerCase().replace(' ', '-');
    }

    private String normalizzaOpzionale(String value) {
        return value == null || value.isBlank() ? "" : value.trim();
    }

    @Override
    @Transactional(readOnly = true)
    public GiocatoreDto getGiocatoreById(long id) {
        Giocatore giocatore = giocatoreDao.getGiocatoreById(id);
        return giocatore == null ? null : giocatoreMapper.fromModel(giocatore);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, GiocatoreDto> selectAll() {
        Map<String, GiocatoreDto> giocatori = new LinkedHashMap<>();
        for (Giocatore giocatore : giocatoreDao.selectAll()) {
            giocatori.put(giocatore.getCodice(), giocatoreMapper.fromModel(giocatore));
        }
        return giocatori;
    }

    @Override
    @Transactional
    public GiocatoreDto addGiocatore(GiocatoreDto dto) {
        return giocatoreMapper.fromModel(giocatoreDao.addGiocatore(preparaGiocatore(dto)));
    }

    @Override
    @Transactional
    public GiocatoreDto updateGiocatore(long id, GiocatoreDto dto) {
        dto.setId(id);
        GiocatoreDto giocatorePreparato = preparaGiocatore(dto);
        Giocatore giocatoreAggiornato = giocatoreDao.updateGiocatore(giocatorePreparato);
        return giocatoreAggiornato == null ? null : giocatoreMapper.fromModel(giocatoreAggiornato);
    }

    @Override
    @Transactional
    public boolean deleteGiocatore(long id) {
        return giocatoreDao.deleteGiocatore(id);
    }
}
