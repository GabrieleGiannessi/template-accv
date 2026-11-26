package com.giannesuite.squadtemplate.mapper;

/** Contratto bidirezionale tra un record/oggetto database e il Model applicativo. */
public interface EntityModelDbMapper<EntityDb, Model> {
    Model toModel(EntityDb entityDb);

    EntityDb fromModel(Model model);
}
