package com.giannesuite.squadtemplate.dao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Competizione {
    private Long id;
    private String codice;
    private String descrizione;
    private String note;
    private String fileLogo;
}
