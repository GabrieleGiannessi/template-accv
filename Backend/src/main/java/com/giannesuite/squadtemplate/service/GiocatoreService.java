package com.giannesuite.squadtemplate.service;

import com.giannesuite.squadtemplate.service.dto.GiocatoreDto;

import java.util.Map;

public interface GiocatoreService {
    GiocatoreDto getGiocatoreById(long id);

    Map<String, GiocatoreDto> selectAll();

    GiocatoreDto addGiocatore(GiocatoreDto dto);

    GiocatoreDto updateGiocatore(long id, GiocatoreDto dto);

    boolean deleteGiocatore(long id);
}
