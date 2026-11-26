package com.giannesuite.squadtemplate.dao.mapper;

import com.giannesuite.squadtemplate.dao.model.Stagione;
import com.giannesuite.squadtemplate.infrastruttura.database.jooq.tables.records.StagioniRecord;
import com.giannesuite.squadtemplate.mapper.EntityModelDbMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StagioneDbMapper extends EntityModelDbMapper<StagioniRecord, Stagione> {
    @Override
    Stagione toModel(StagioniRecord record);

    @Override
    @Mapping(target = "id", ignore = true)
    StagioniRecord fromModel(Stagione stagione);
}
