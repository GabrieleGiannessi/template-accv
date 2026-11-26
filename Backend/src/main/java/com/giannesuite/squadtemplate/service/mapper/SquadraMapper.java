package com.giannesuite.squadtemplate.service.mapper;

import com.giannesuite.squadtemplate.service.dto.SquadraDto;
import com.giannesuite.squadtemplate.mapper.EntityDtoMapper;
import com.giannesuite.squadtemplate.dao.model.Squadra;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SquadraMapper extends EntityDtoMapper<SquadraDto, Squadra> {
    List<Integer> COLORE_PRIMARIO_DEFAULT = List.of(16, 185, 129);
    List<Integer> COLORE_SECONDARIO_DEFAULT = List.of(239, 68, 68);
    List<Integer> COLORE_TERZIARIO_DEFAULT = List.of(59, 130, 246);

    @Override
    @Mapping(target = "codice", source = "key")
    @Mapping(target = "nome", source = "name")
    @Mapping(target = "nomeEsteso", source = "extendedName")
    @Mapping(target = "nomeBreve", source = "shortName")
    @Mapping(target = "colorePrimario", source = "primaryColor", qualifiedByName = "primarioToHex")
    @Mapping(target = "coloreSecondario", source = "secondaryColor", qualifiedByName = "secondarioToHex")
    @Mapping(target = "coloreTerziario", source = "tertiaryColor", qualifiedByName = "terziarioToHex")
    @Mapping(target = "note", source = "notes")
    @Mapping(target = "fileLogo", source = "logoFilename")
    @Mapping(target = "rivalita", source = "rivalry")
    @Mapping(target = "id", ignore = true)
    Squadra toModel(SquadraDto dto);

    @Override
    @Mapping(target = "key", source = "codice")
    @Mapping(target = "name", source = "nome")
    @Mapping(target = "extendedName", source = "nomeEsteso")
    @Mapping(target = "shortName", source = "nomeBreve")
    @Mapping(target = "primaryColor", source = "colorePrimario", qualifiedByName = "primarioToRgb")
    @Mapping(target = "secondaryColor", source = "coloreSecondario", qualifiedByName = "secondarioToRgb")
    @Mapping(target = "tertiaryColor", source = "coloreTerziario", qualifiedByName = "terziarioToRgb")
    @Mapping(target = "notes", source = "note")
    @Mapping(target = "logoFilename", source = "fileLogo")
    @Mapping(target = "rivalry", source = "rivalita")
    SquadraDto fromModel(Squadra squadra);

    @Named("primarioToRgb")
    default List<Integer> primarioToRgb(String value) {
        return toRgb(value, COLORE_PRIMARIO_DEFAULT);
    }

    @Named("secondarioToRgb")
    default List<Integer> secondarioToRgb(String value) {
        return toRgb(value, COLORE_SECONDARIO_DEFAULT);
    }

    @Named("terziarioToRgb")
    default List<Integer> terziarioToRgb(String value) {
        return toRgb(value, COLORE_TERZIARIO_DEFAULT);
    }

    @Named("primarioToHex")
    default String primarioToHex(List<Integer> value) {
        return toHex(value, COLORE_PRIMARIO_DEFAULT);
    }

    @Named("secondarioToHex")
    default String secondarioToHex(List<Integer> value) {
        return toHex(value, COLORE_SECONDARIO_DEFAULT);
    }

    @Named("terziarioToHex")
    default String terziarioToHex(List<Integer> value) {
        return toHex(value, COLORE_TERZIARIO_DEFAULT);
    }

    private List<Integer> toRgb(String hex, List<Integer> fallback) {
        if (hex == null || !hex.matches("#[0-9A-Fa-f]{6}")) {
            return fallback;
        }
        return List.of(
                Integer.parseInt(hex.substring(1, 3), 16),
                Integer.parseInt(hex.substring(3, 5), 16),
                Integer.parseInt(hex.substring(5, 7), 16)
        );
    }

    private String toHex(List<Integer> rgb, List<Integer> fallback) {
        List<Integer> color = rgb == null ? fallback : rgb;
        if (color.size() != 3) {
            throw new IllegalArgumentException("Un colore deve contenere tre valori RGB");
        }
        for (Integer channel : color) {
            if (channel == null || channel < 0 || channel > 255) {
                throw new IllegalArgumentException("Ogni canale RGB deve essere compreso tra 0 e 255");
            }
        }
        return String.format("#%02X%02X%02X", color.get(0), color.get(1), color.get(2));
    }
}
