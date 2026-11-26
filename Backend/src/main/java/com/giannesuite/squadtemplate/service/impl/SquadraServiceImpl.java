package com.giannesuite.squadtemplate.service.impl;

import com.giannesuite.squadtemplate.dao.SquadraDao;
import com.giannesuite.squadtemplate.dao.model.Squadra;
import com.giannesuite.squadtemplate.service.SquadraService;
import com.giannesuite.squadtemplate.service.dto.SquadraDto;
import com.giannesuite.squadtemplate.service.mapper.SquadraMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SquadraServiceImpl implements SquadraService {
    @Autowired
    private SquadraDao squadraDao;
    @Autowired
    private SquadraMapper squadraMapper;

    private SquadraDto preparaSquadra(SquadraDto dto) {
        String codice = normalizzaCodice(dto.getKey());
        if (codice.isBlank()) {
            throw new IllegalArgumentException("Il codice squadra è obbligatorio");
        }

        dto.setKey(codice);
        dto.setName(dto.getName().trim());
        dto.setExtendedName(emptyIfNull(dto.getExtendedName()));
        dto.setNotes(emptyIfNull(dto.getNotes()));

        if (dto.getShortName() == null || dto.getShortName().isBlank()) {
            dto.setShortName(dto.getName().substring(0, Math.min(3, dto.getName().length())).toUpperCase());
        }

        if ("accv".equals(codice)) {
            dto.setRivalry(null);
        } else if (dto.getRivalry() == null) {
            dto.setRivalry(3);
        }
        return dto;
    }

    private String normalizzaCodice(String value) {
        return value == null ? "" : value.trim().toLowerCase().replace(' ', '_');
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    @Transactional(readOnly = true)
    public SquadraDto getSquadraById(long id) {
        Squadra squadra = squadraDao.getSquadraById(id);
        return squadra == null ? null : squadraMapper.fromModel(squadra);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, SquadraDto> selectAll() {
        Map<String, SquadraDto> squadre = new LinkedHashMap<>();
        for (Squadra squadra : squadraDao.selectAll()) {
            squadre.put(squadra.getCodice(), squadraMapper.fromModel(squadra));
        }
        return squadre;
    }

    @Override
    @Transactional
    public SquadraDto addSquadra(SquadraDto dto) {
        return squadraMapper.fromModel(squadraDao.addSquadra(preparaSquadra(dto)));
    }

    @Override
    @Transactional
    public SquadraDto updateSquadra(long id, SquadraDto dto) {
        dto.setId(id);
        SquadraDto squadraPreparata = preparaSquadra(dto);
        Squadra squadraAggiornata = squadraDao.updateSquadra(squadraPreparata);
        return squadraAggiornata == null ? null : squadraMapper.fromModel(squadraAggiornata);
    }

    @Override
    @Transactional
    public boolean deleteSquadra(long id) {
        return squadraDao.deleteSquadraById(id);
    }

    @Override
    @Transactional
    public boolean deleteSquadraByCodice(String codice) {
        return squadraDao.deleteSquadraByCodice(normalizzaCodice(codice));
    }

}
