package com.giannesuite.squadtemplate.service.mapper;

import com.giannesuite.squadtemplate.dao.model.Competizione;
import com.giannesuite.squadtemplate.mapper.EntityDtoMapper;
import com.giannesuite.squadtemplate.service.dto.CompetizioneDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompetizioneMapper extends EntityDtoMapper<CompetizioneDto, Competizione> {
    @Override
    @Mapping(target = "codice", source = "key")
    @Mapping(target = "descrizione", source = "description")
    @Mapping(target = "note", source = "notes")
    @Mapping(target = "fileLogo", source = "logoFilename")
    Competizione toModel(CompetizioneDto dto);

    @Override
    @Mapping(target = "key", source = "codice")
    @Mapping(target = "description", source = "descrizione")
    @Mapping(target = "notes", source = "note")
    @Mapping(target = "logoFilename", source = "fileLogo")
    @Mapping(target = "seasons", ignore = true)
    CompetizioneDto fromModel(Competizione competizione);
}
