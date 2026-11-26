package com.giannesuite.squadtemplate.dao;

import com.giannesuite.squadtemplate.dao.model.Squadra;
import com.giannesuite.squadtemplate.service.dto.SquadraDto;

import java.util.List;

public interface SquadraDao {
    Squadra getSquadraById(long id);

    Squadra getSquadraByCodice(String codice);

    List<Squadra> selectAll();

    Squadra addSquadra(SquadraDto dto);

    Squadra updateSquadra(SquadraDto dto);

    boolean deleteSquadraById(long id);

    boolean deleteSquadraByCodice(String codice);
}
