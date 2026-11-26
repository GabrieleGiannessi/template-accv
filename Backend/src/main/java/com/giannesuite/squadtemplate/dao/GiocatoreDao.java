package com.giannesuite.squadtemplate.dao;

import com.giannesuite.squadtemplate.dao.model.Giocatore;
import com.giannesuite.squadtemplate.service.dto.GiocatoreDto;

import java.util.List;

public interface GiocatoreDao {
    Giocatore getGiocatoreById(long id);

    Giocatore getGiocatoreByCodice(String codice);

    List<Giocatore> selectAll();

    Giocatore addGiocatore(GiocatoreDto dto);

    Giocatore updateGiocatore(GiocatoreDto dto);

    boolean deleteGiocatore(long id);
}
