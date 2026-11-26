package com.giannesuite.squadtemplate.dao.impl;

import com.giannesuite.squadtemplate.dao.StagioneDao;
import com.giannesuite.squadtemplate.dao.mapper.StagioneDbMapper;
import com.giannesuite.squadtemplate.dao.model.Stagione;
import com.giannesuite.squadtemplate.infrastruttura.database.jooq.tables.records.StagioniRecord;
import com.giannesuite.squadtemplate.service.dto.StagioneDto;
import com.giannesuite.squadtemplate.service.mapper.StagioneMapper;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.giannesuite.squadtemplate.infrastruttura.database.jooq.tables.Stagioni.STAGIONI;

@Component
public class StagioneDaoImpl implements StagioneDao {
    @Autowired
    private DSLContext create;
    @Autowired
    private StagioneDbMapper stagioneDbMapper;
    @Autowired
    private StagioneMapper stagioneMapper;

    private Stagione getInstance(StagioniRecord record) {
        return record == null ? null : stagioneDbMapper.toModel(record);
    }

    private Stagione toModel(StagioneDto dto) {
        return stagioneMapper.toModel(dto);
    }

    private void store(StagioniRecord record, StagioneDto dto) {
        Stagione stagione = toModel(dto);
        record.setCodice(stagione.getCodice());
        record.setDescrizione(stagione.getDescrizione());
        record.setNote(stagione.getNote());
        record.store();
    }

    @Override
    public Stagione getStagioneById(long id) {
        return create.selectFrom(STAGIONI)
                .where(STAGIONI.ID.eq(id))
                .fetchOne(this::getInstance);
    }

    @Override
    public Stagione getStagioneByCodice(String codice) {
        return create.selectFrom(STAGIONI)
                .where(STAGIONI.CODICE.eq(codice))
                .fetchOne(this::getInstance);
    }

    @Override
    public List<Stagione> selectAll() {
        return create.selectFrom(STAGIONI)
                .orderBy(STAGIONI.CODICE.desc())
                .fetch().stream()
                .map(this::getInstance)
                .toList();
    }

    @Override
    public Stagione addStagione(StagioneDto dto) {
        Stagione stagione = toModel(dto);
        StagioniRecord record = create.newRecord(STAGIONI);
        store(record, dto);
        Stagione stagioneInserita = getStagioneByCodice(stagione.getCodice());
        if (stagioneInserita == null) {
            throw new IllegalStateException("La stagione inserita non è stata trovata");
        }
        return stagioneInserita;
    }

    @Override
    public Stagione updateStagione(StagioneDto dto) {
        StagioniRecord record = create.fetchOne(STAGIONI, STAGIONI.ID.eq(dto.getId()));
        if (record == null) return null;
        store(record, dto);
        return getStagioneById(dto.getId());
    }

    @Override
    public boolean deleteStagione(long id) {
        return create.deleteFrom(STAGIONI)
                .where(STAGIONI.ID.eq(id))
                .execute() > 0;
    }
}
