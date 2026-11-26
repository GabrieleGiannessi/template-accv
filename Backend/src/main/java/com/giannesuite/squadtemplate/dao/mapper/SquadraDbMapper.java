package com.giannesuite.squadtemplate.dao.mapper;

import com.giannesuite.squadtemplate.infrastruttura.database.jooq.tables.records.SquadreRecord;
import com.giannesuite.squadtemplate.mapper.EntityModelDbMapper;
import com.giannesuite.squadtemplate.dao.model.Squadra;
import org.jooq.types.UByte;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface SquadraDbMapper extends EntityModelDbMapper<SquadreRecord, Squadra> {
    @Override
    @Mapping(target = "rivalita", source = "rivalita", qualifiedByName = "ubyteToInteger")
    Squadra toModel(SquadreRecord record);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rivalita", source = "rivalita", qualifiedByName = "integerToUbyte")
    SquadreRecord fromModel(Squadra squadra);

    @Named("ubyteToInteger")
    default Integer ubyteToInteger(UByte value) {
        return value == null ? null : value.intValue();
    }

    @Named("integerToUbyte")
    default UByte integerToUbyte(Integer value) {
        return value == null ? null : UByte.valueOf(value);
    }
}
