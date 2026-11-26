package com.giannesuite.squadtemplate.mapper;

/** Contratto bidirezionale per la conversione tra DTO e Model applicativi. */
public interface EntityDtoMapper<EntityDto, Model> {
    Model toModel(EntityDto dto);

    EntityDto fromModel(Model model);
}
