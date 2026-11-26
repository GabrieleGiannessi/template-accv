package com.giannesuite.squadtemplate.service;

import com.giannesuite.squadtemplate.service.dto.CompetizioneDto;
import com.giannesuite.squadtemplate.service.dto.CompetizioneStagioneDto;

import java.util.List;
import java.util.Map;

public interface CompetizioneService {
    CompetizioneDto getCompetizioneById(long id);

    Map<String, CompetizioneDto> selectAll();

    CompetizioneDto addCompetizione(CompetizioneDto dto);

    CompetizioneDto updateCompetizione(long id, CompetizioneDto dto);

    boolean deleteCompetizione(long id);

    List<CompetizioneStagioneDto> getAssociazioni(long idCompetizione);

    List<CompetizioneStagioneDto> replaceAssociazioni(long idCompetizione, List<CompetizioneStagioneDto> associazioni);
}
