package com.giannesuite.squadtemplate.dao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Giocatore {
    private Long id;
    private String codice;
    private String nome;
    private String nomeMostrato;
    private String ruolo;
    private String numeroMaglia;
    private Long idSquadra;
    private String fileFoto;
}
