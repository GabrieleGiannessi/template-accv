package com.giannesuite.squadtemplate.service.mapper;

import com.giannesuite.squadtemplate.dao.model.Giocatore;
import com.giannesuite.squadtemplate.mapper.EntityDtoMapper;
import com.giannesuite.squadtemplate.service.dto.GiocatoreDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GiocatoreMapper extends EntityDtoMapper<GiocatoreDto, Giocatore> {
    @Override
    @Mapping(target = "codice", source = "key")
    @Mapping(target = "nome", source = "name")
    @Mapping(target = "nomeMostrato", source = "displayName")
    @Mapping(target = "ruolo", source = "role")
    @Mapping(target = "numeroMaglia", source = "number")
    @Mapping(target = "idSquadra", source = "teamId")
    @Mapping(target = "fileFoto", source = "photoFilename")
    @Mapping(target = "id", ignore = true)
    Giocatore toModel(GiocatoreDto dto);

    @Override
    @Mapping(target = "key", source = "codice")
    @Mapping(target = "name", source = "nome")
    @Mapping(target = "displayName", source = "nomeMostrato")
    @Mapping(target = "role", source = "ruolo")
    @Mapping(target = "number", source = "numeroMaglia")
    @Mapping(target = "teamId", source = "idSquadra")
    @Mapping(target = "photoFilename", source = "fileFoto")
    GiocatoreDto fromModel(Giocatore giocatore);
}
