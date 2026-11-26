package com.giannesuite.squadtemplate.dao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Squadra {
    private Long id;
    private String codice;
    private String nome;
    private String nomeEsteso;
    private String nomeBreve;
    private String colorePrimario;
    private String coloreSecondario;
    private String coloreTerziario;
    private String note;
    private String fileLogo;
    private Integer rivalita;
}
