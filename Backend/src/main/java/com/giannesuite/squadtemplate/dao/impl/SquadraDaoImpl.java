package com.giannesuite.squadtemplate.dao.impl;

import com.giannesuite.squadtemplate.dao.SquadraDao;
import com.giannesuite.squadtemplate.dao.mapper.SquadraDbMapper;
import com.giannesuite.squadtemplate.dao.model.Squadra;
import com.giannesuite.squadtemplate.infrastruttura.database.jooq.tables.records.SquadreRecord;
import com.giannesuite.squadtemplate.service.dto.SquadraDto;
import com.giannesuite.squadtemplate.service.mapper.SquadraMapper;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.giannesuite.squadtemplate.infrastruttura.database.jooq.tables.Squadre.SQUADRE;

@Component
public class SquadraDaoImpl implements SquadraDao {
    @Autowired
    private DSLContext create;
    @Autowired
    private SquadraDbMapper squadraDbMapper;
    @Autowired
    private SquadraMapper squadraMapper;

    private Squadra getInstance(SquadreRecord record) {
        return record == null ? null : squadraDbMapper.toModel(record);
    }

    private Squadra toModel(SquadraDto dto) {
        return squadraMapper.toModel(dto);
    }

    private void store(SquadreRecord record, SquadraDto dto) {
        Squadra squadra = toModel(dto);
        record.setCodice(squadra.getCodice());
        record.setNome(squadra.getNome());
        record.setNomeEsteso(squadra.getNomeEsteso());
        record.setNomeBreve(squadra.getNomeBreve());
        record.setColorePrimario(squadra.getColorePrimario());
        record.setColoreSecondario(squadra.getColoreSecondario());
        record.setColoreTerziario(squadra.getColoreTerziario());
        record.setNote(squadra.getNote());
        record.setFileLogo(squadra.getFileLogo());
        record.setRivalita(squadra.getRivalita() == null
                ? null
                : org.jooq.types.UByte.valueOf(squadra.getRivalita()));
        record.store();
    }

    @Override
    public Squadra getSquadraById(long id) {
        return create.selectFrom(SQUADRE)
                .where(SQUADRE.ID.eq(id))
                .fetchOne(this::getInstance);
    }

    @Override
    public Squadra getSquadraByCodice(String codice) {
        return create.selectFrom(SQUADRE)
                .where(SQUADRE.CODICE.eq(codice))
                .fetchOne(this::getInstance);
    }

    @Override
    public List<Squadra> selectAll() {
        return create.selectFrom(SQUADRE)
                .orderBy(SQUADRE.CODICE.asc())
                .fetch().stream()
                .map(this::getInstance)
                .toList();
    }

    @Override
    public Squadra addSquadra(SquadraDto dto) {
        Squadra squadra = toModel(dto);
        SquadreRecord record = create.newRecord(SQUADRE);
        store(record, dto);

        Squadra squadraInserita = getSquadraByCodice(squadra.getCodice());
        if (squadraInserita == null) {
            throw new IllegalStateException("La squadra inserita non è stata trovata");
        }
        return squadraInserita;
    }

    @Override
    public Squadra updateSquadra(SquadraDto dto) {
        SquadreRecord record = create.fetchOne(SQUADRE, SQUADRE.ID.eq(dto.getId()));
        if (record == null) return null;
        store(record, dto);
        return getSquadraById(dto.getId());
    }

    @Override
    public boolean deleteSquadraById(long id) {
        return create.deleteFrom(SQUADRE)
                .where(SQUADRE.ID.eq(id))
                .execute() > 0;
    }

    @Override
    public boolean deleteSquadraByCodice(String codice) {
        return create.deleteFrom(SQUADRE)
                .where(SQUADRE.CODICE.eq(codice))
                .execute() > 0;
    }
}
