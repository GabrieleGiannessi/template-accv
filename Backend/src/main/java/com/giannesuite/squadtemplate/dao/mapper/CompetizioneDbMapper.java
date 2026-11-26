package com.giannesuite.squadtemplate.dao.mapper;

import com.giannesuite.squadtemplate.dao.model.Competizione;
import com.giannesuite.squadtemplate.infrastruttura.database.jooq.tables.records.CompetizioniRecord;
import com.giannesuite.squadtemplate.mapper.EntityModelDbMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompetizioneDbMapper extends EntityModelDbMapper<CompetizioniRecord, Competizione> {
    @Override
    Competizione toModel(CompetizioniRecord record);

    @Override
    @Mapping(target = "id", ignore = true)
    CompetizioniRecord fromModel(Competizione competizione);
}
