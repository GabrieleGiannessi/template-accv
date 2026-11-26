package com.giannesuite.squadtemplate.service;

import com.giannesuite.squadtemplate.service.dto.SquadraDto;

import java.util.Map;

public interface SquadraService {
    SquadraDto getSquadraById(long id);

    Map<String, SquadraDto> selectAll();

    SquadraDto addSquadra(SquadraDto dto);

    SquadraDto updateSquadra(long id, SquadraDto dto);

    boolean deleteSquadra(long id);

    boolean deleteSquadraByCodice(String codice);
}
