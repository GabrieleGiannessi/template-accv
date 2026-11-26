package com.giannesuite.squadtemplate.dao.mapper;

import com.giannesuite.squadtemplate.dao.model.Giocatore;
import com.giannesuite.squadtemplate.infrastruttura.database.jooq.tables.records.GiocatoriRecord;
import com.giannesuite.squadtemplate.mapper.EntityModelDbMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GiocatoreDbMapper extends EntityModelDbMapper<GiocatoriRecord, Giocatore> {
    @Override
    Giocatore toModel(GiocatoriRecord record);

    @Override
    @Mapping(target = "id", ignore = true)
    GiocatoriRecord fromModel(Giocatore giocatore);
}
