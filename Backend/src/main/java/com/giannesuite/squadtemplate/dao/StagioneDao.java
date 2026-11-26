package com.giannesuite.squadtemplate.dao;

import com.giannesuite.squadtemplate.dao.model.Stagione;
import com.giannesuite.squadtemplate.service.dto.StagioneDto;

import java.util.List;

public interface StagioneDao {
    Stagione getStagioneById(long id);

    Stagione getStagioneByCodice(String codice);

    List<Stagione> selectAll();

    Stagione addStagione(StagioneDto dto);

    Stagione updateStagione(StagioneDto dto);

    boolean deleteStagione(long id);
}
