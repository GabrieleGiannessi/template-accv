package com.giannesuite.squadtemplate.dao.impl;

import com.giannesuite.squadtemplate.dao.GiocatoreDao;
import com.giannesuite.squadtemplate.dao.mapper.GiocatoreDbMapper;
import com.giannesuite.squadtemplate.dao.model.Giocatore;
import com.giannesuite.squadtemplate.infrastruttura.database.jooq.tables.records.GiocatoriRecord;
import com.giannesuite.squadtemplate.service.dto.GiocatoreDto;
import com.giannesuite.squadtemplate.service.mapper.GiocatoreMapper;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.giannesuite.squadtemplate.infrastruttura.database.jooq.tables.Giocatori.GIOCATORI;

@Component
public class GiocatoreDaoImpl implements GiocatoreDao {
    @Autowired
    private DSLContext create;
    @Autowired
    private GiocatoreDbMapper giocatoreDbMapper;
    @Autowired
    private GiocatoreMapper giocatoreMapper;

    private Giocatore getInstance(GiocatoriRecord record) {
        return record == null ? null : giocatoreDbMapper.toModel(record);
    }

    private Giocatore toModel(GiocatoreDto dto) {
        return giocatoreMapper.toModel(dto);
    }

    private void store(GiocatoriRecord record, GiocatoreDto dto) {
        Giocatore giocatore = toModel(dto);
        record.setCodice(giocatore.getCodice());
        record.setNome(giocatore.getNome());
        record.setNomeMostrato(giocatore.getNomeMostrato());
        record.setRuolo(giocatore.getRuolo());
        record.setNumeroMaglia(giocatore.getNumeroMaglia());
        record.setIdSquadra(giocatore.getIdSquadra());
        record.setFileFoto(giocatore.getFileFoto());
        record.store();
    }

    @Override
    public Giocatore getGiocatoreById(long id) {
        return create.selectFrom(GIOCATORI)
                .where(GIOCATORI.ID.eq(id))
                .fetchOne(this::getInstance);
    }

    @Override
    public Giocatore getGiocatoreByCodice(String codice) {
        return create.selectFrom(GIOCATORI)
                .where(GIOCATORI.CODICE.eq(codice))
                .fetchOne(this::getInstance);
    }

    @Override
    public List<Giocatore> selectAll() {
        return create.selectFrom(GIOCATORI)
                .orderBy(GIOCATORI.CODICE.asc())
                .fetch().stream()
                .map(this::getInstance)
                .toList();
    }

    @Override
    public Giocatore addGiocatore(GiocatoreDto dto) {
        Giocatore giocatore = toModel(dto);
        GiocatoriRecord record = create.newRecord(GIOCATORI);
        store(record, dto);
        Giocatore giocatoreInserito = getGiocatoreByCodice(giocatore.getCodice());
        if (giocatoreInserito == null) {
            throw new IllegalStateException("Il giocatore inserito non è stato trovato");
        }
        return giocatoreInserito;
    }

    @Override
    public Giocatore updateGiocatore(GiocatoreDto dto) {
        GiocatoriRecord record = create.fetchOne(GIOCATORI, GIOCATORI.ID.eq(dto.getId()));
        if (record == null) return null;
        store(record, dto);
        return getGiocatoreById(dto.getId());
    }

    @Override
    public boolean deleteGiocatore(long id) {
        return create.deleteFrom(GIOCATORI)
                .where(GIOCATORI.ID.eq(id))
                .execute() > 0;
    }
}
