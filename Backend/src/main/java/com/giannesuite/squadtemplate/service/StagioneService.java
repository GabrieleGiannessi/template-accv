package com.giannesuite.squadtemplate.service;

import com.giannesuite.squadtemplate.service.dto.StagioneDto;

import java.util.Map;

public interface StagioneService {
    StagioneDto getStagioneById(long id);

    Map<String, StagioneDto> selectAll();

    StagioneDto addStagione(StagioneDto dto);

    StagioneDto updateStagione(long id, StagioneDto dto);

    boolean deleteStagione(long id);
}
