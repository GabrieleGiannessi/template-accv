package com.giannesuite.squadtemplate.service.mapper;

import com.giannesuite.squadtemplate.dao.model.Stagione;
import com.giannesuite.squadtemplate.mapper.EntityDtoMapper;
import com.giannesuite.squadtemplate.service.dto.StagioneDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StagioneMapper extends EntityDtoMapper<StagioneDto, Stagione> {
    @Override
    @Mapping(target = "codice", source = "key")
    @Mapping(target = "descrizione", source = "description")
    @Mapping(target = "note", source = "notes")
    Stagione toModel(StagioneDto dto);

    @Override
    @Mapping(target = "key", source = "codice")
    @Mapping(target = "description", source = "descrizione")
    @Mapping(target = "notes", source = "note")
    StagioneDto fromModel(Stagione stagione);
}
