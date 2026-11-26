package com.giannesuite.squadtemplate.dao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stagione {
    private Long id;
    private String codice;
    private String descrizione;
    private String note;
}
