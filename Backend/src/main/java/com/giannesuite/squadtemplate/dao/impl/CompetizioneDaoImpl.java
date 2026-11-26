package com.giannesuite.squadtemplate.dao.impl;

import com.giannesuite.squadtemplate.dao.CompetizioneDao;
import com.giannesuite.squadtemplate.dao.mapper.CompetizioneDbMapper;
import com.giannesuite.squadtemplate.dao.model.Competizione;
import com.giannesuite.squadtemplate.infrastruttura.database.jooq.tables.records.CompetizioniRecord;
import com.giannesuite.squadtemplate.service.dto.CompetizioneDto;
import com.giannesuite.squadtemplate.service.dto.CompetizioneStagioneDto;
import com.giannesuite.squadtemplate.service.mapper.CompetizioneMapper;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.giannesuite.squadtemplate.infrastruttura.database.jooq.tables.Competizioni.COMPETIZIONI;
import static com.giannesuite.squadtemplate.infrastruttura.database.jooq.tables.CompetizioniStagioniSquadre.COMPETIZIONI_STAGIONI_SQUADRE;
import static com.giannesuite.squadtemplate.infrastruttura.database.jooq.tables.Squadre.SQUADRE;
import static com.giannesuite.squadtemplate.infrastruttura.database.jooq.tables.Stagioni.STAGIONI;

@Component
public class CompetizioneDaoImpl implements CompetizioneDao {
    @Autowired
    private DSLContext create;
    @Autowired
    private CompetizioneDbMapper competizioneDbMapper;
    @Autowired
    private CompetizioneMapper competizioneMapper;

    private Competizione getInstance(CompetizioniRecord record) {
        return record == null ? null : competizioneDbMapper.toModel(record);
    }

    private Competizione toModel(CompetizioneDto dto) {
        return competizioneMapper.toModel(dto);
    }

    private void store(CompetizioniRecord record, CompetizioneDto dto) {
        Competizione competizione = toModel(dto);
        record.setCodice(competizione.getCodice());
        record.setDescrizione(competizione.getDescrizione());
        record.setNote(competizione.getNote());
        record.setFileLogo(competizione.getFileLogo());
        record.store();
    }

    @Override
    public Competizione getCompetizioneById(long id) {
        return create.selectFrom(COMPETIZIONI)
                .where(COMPETIZIONI.ID.eq(id))
                .fetchOne(this::getInstance);
    }

    @Override
    public Competizione getCompetizioneByCodice(String codice) {
        return create.selectFrom(COMPETIZIONI)
                .where(COMPETIZIONI.CODICE.eq(codice))
                .fetchOne(this::getInstance);
    }

    @Override
    public List<Competizione> selectAll() {
        return create.selectFrom(COMPETIZIONI)
                .orderBy(COMPETIZIONI.CODICE.asc())
                .fetch().stream()
                .map(this::getInstance)
                .toList();
    }

    @Override
    public Competizione addCompetizione(CompetizioneDto dto) {
        Competizione competizione = toModel(dto);
        CompetizioniRecord record = create.newRecord(COMPETIZIONI);
        store(record, dto);
        Competizione competizioneInserita = getCompetizioneByCodice(competizione.getCodice());
        if (competizioneInserita == null) {
            throw new IllegalStateException("La competizione inserita non è stata trovata");
        }
        return competizioneInserita;
    }

    @Override
    public Competizione updateCompetizione(CompetizioneDto dto) {
        CompetizioniRecord record = create.fetchOne(COMPETIZIONI, COMPETIZIONI.ID.eq(dto.getId()));
        if (record == null) return null;
        store(record, dto);
        return getCompetizioneById(dto.getId());
    }

    @Override
    public boolean deleteCompetizione(long id) {
        return create.deleteFrom(COMPETIZIONI)
                .where(COMPETIZIONI.ID.eq(id))
                .execute() > 0;
    }

    @Override
    public List<CompetizioneStagioneDto> selectAssociazioni(long idCompetizione) {
        Map<String, CompetizioneStagioneDto> perStagione = new LinkedHashMap<>();
        create.select(STAGIONI.CODICE, SQUADRE.CODICE)
                .from(COMPETIZIONI_STAGIONI_SQUADRE)
                .join(STAGIONI).on(STAGIONI.ID.eq(COMPETIZIONI_STAGIONI_SQUADRE.ID_STAGIONE))
                .join(SQUADRE).on(SQUADRE.ID.eq(COMPETIZIONI_STAGIONI_SQUADRE.ID_SQUADRA))
                .where(COMPETIZIONI_STAGIONI_SQUADRE.ID_COMPETIZIONE.eq(idCompetizione))
                .orderBy(STAGIONI.CODICE.asc(), SQUADRE.CODICE.asc())
                .fetch()
                .forEach(record -> {
                    String codiceStagione = record.get(STAGIONI.CODICE);
                    CompetizioneStagioneDto dto = perStagione.computeIfAbsent(codiceStagione, codice -> {
                        CompetizioneStagioneDto nuova = new CompetizioneStagioneDto();
                        nuova.setSeasonKey(codice);
                        return nuova;
                    });
                    dto.getTeamKeys().add(record.get(SQUADRE.CODICE));
                });
        return new ArrayList<>(perStagione.values());
    }

    @Override
    public void replaceAssociazioni(long idCompetizione, List<CompetizioneStagioneDto> associazioni) {
        if (getCompetizioneById(idCompetizione) == null) {
            throw new IllegalArgumentException("Competizione non trovata");
        }

        List<Query> queryInserimento = new ArrayList<>();
        Set<String> associazioniGiaViste = new LinkedHashSet<>();
        for (CompetizioneStagioneDto associazione : associazioni) {
            if (associazione.getSeasonKey() == null || associazione.getSeasonKey().isBlank()) {
                throw new IllegalArgumentException("Il codice stagione è obbligatorio per ogni associazione");
            }
            if (associazione.getTeamKeys() == null) {
                throw new IllegalArgumentException("L'elenco delle squadre associate è obbligatorio");
            }
            Long idStagione = create.select(STAGIONI.ID)
                    .from(STAGIONI)
                    .where(STAGIONI.CODICE.eq(associazione.getSeasonKey()))
                    .fetchOne(STAGIONI.ID);
            if (idStagione == null) {
                throw new IllegalArgumentException("Stagione non trovata: " + associazione.getSeasonKey());
            }
            for (String codiceSquadra : associazione.getTeamKeys()) {
                if (codiceSquadra == null || codiceSquadra.isBlank()) {
                    throw new IllegalArgumentException("Il codice squadra è obbligatorio per ogni associazione");
                }
                if (!associazioniGiaViste.add(associazione.getSeasonKey() + "\u0000" + codiceSquadra)) {
                    continue;
                }
                Long idSquadra = create.select(SQUADRE.ID)
                        .from(SQUADRE)
                        .where(SQUADRE.CODICE.eq(codiceSquadra))
                        .fetchOne(SQUADRE.ID);
                if (idSquadra == null) {
                    throw new IllegalArgumentException("Squadra non trovata: " + codiceSquadra);
                }
                queryInserimento.add(create.insertInto(COMPETIZIONI_STAGIONI_SQUADRE)
                        .set(COMPETIZIONI_STAGIONI_SQUADRE.ID_COMPETIZIONE, idCompetizione)
                        .set(COMPETIZIONI_STAGIONI_SQUADRE.ID_STAGIONE, idStagione)
                        .set(COMPETIZIONI_STAGIONI_SQUADRE.ID_SQUADRA, idSquadra));
            }
        }

        create.deleteFrom(COMPETIZIONI_STAGIONI_SQUADRE)
                .where(COMPETIZIONI_STAGIONI_SQUADRE.ID_COMPETIZIONE.eq(idCompetizione))
                .execute();
        if (!queryInserimento.isEmpty()) {
            create.batch(queryInserimento).execute();
        }
    }
}
