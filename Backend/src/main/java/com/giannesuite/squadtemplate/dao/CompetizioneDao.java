package com.giannesuite.squadtemplate.dao;

import com.giannesuite.squadtemplate.dao.model.Competizione;
import com.giannesuite.squadtemplate.service.dto.CompetizioneDto;
import com.giannesuite.squadtemplate.service.dto.CompetizioneStagioneDto;

import java.util.List;

public interface CompetizioneDao {
    Competizione getCompetizioneById(long id);

    Competizione getCompetizioneByCodice(String codice);

    List<Competizione> selectAll();

    Competizione addCompetizione(CompetizioneDto dto);

    Competizione updateCompetizione(CompetizioneDto dto);

    boolean deleteCompetizione(long id);

    List<CompetizioneStagioneDto> selectAssociazioni(long idCompetizione);

    void replaceAssociazioni(long idCompetizione, List<CompetizioneStagioneDto> associazioni);
}
