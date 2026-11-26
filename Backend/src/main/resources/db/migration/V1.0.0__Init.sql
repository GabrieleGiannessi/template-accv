-- BOZZA DA REVISIONARE: questo file non si trova nel percorso caricato da Flyway.
-- Dopo l'approvazione, spostarlo in src/main/resources/db/migration/.
--
-- Modello iniziale ricavato da data/players.json, teams.json, seasons.json
-- e competitions.json. Non contiene dati iniziali: l'importazione dei JSON
-- sarà definita separatamente dopo la revisione dello schema.

CREATE TABLE squadre (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codice VARCHAR(64) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    nome_esteso VARCHAR(160) NOT NULL DEFAULT '',
    nome_breve VARCHAR(8) NOT NULL DEFAULT '',
    colore_primario CHAR(7) NOT NULL DEFAULT '#10B981',
    colore_secondario CHAR(7) NOT NULL DEFAULT '#EF4444',
    colore_terziario CHAR(7) NOT NULL DEFAULT '#3B82F6',
    note TEXT NULL,
    file_logo VARCHAR(255) NULL,
    rivalita TINYINT UNSIGNED NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_squadre_codice UNIQUE (codice)
);

CREATE TABLE stagioni (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codice VARCHAR(80) NOT NULL,
    descrizione VARCHAR(120) NOT NULL,
    note TEXT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_stagioni_codice UNIQUE (codice)
);

CREATE TABLE competizioni (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codice VARCHAR(80) NOT NULL,
    descrizione VARCHAR(160) NOT NULL,
    note TEXT NULL,
    file_logo VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_competizioni_codice UNIQUE (codice)
);

CREATE TABLE giocatori (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codice VARCHAR(80) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    nome_mostrato VARCHAR(120) NULL,
    ruolo VARCHAR(40) NOT NULL,
    numero_maglia VARCHAR(10) NULL,
    id_squadra BIGINT NOT NULL,
    file_foto VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_giocatori_codice UNIQUE (codice),
    INDEX ix_giocatori_squadra (id_squadra),
    CONSTRAINT fk_giocatori_squadre
        FOREIGN KEY (id_squadra) REFERENCES squadre (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT
);

-- Una competizione può avere squadre diverse in ogni stagione.
CREATE TABLE competizioni_stagioni_squadre (
    id_competizione BIGINT NOT NULL,
    id_stagione BIGINT NOT NULL,
    id_squadra BIGINT NOT NULL,
    PRIMARY KEY (id_competizione, id_stagione, id_squadra),
    INDEX ix_comp_stag_squadre_stagione (id_stagione),
    INDEX ix_comp_stag_squadre_squadra (id_squadra),
    CONSTRAINT fk_comp_stag_squadre_competizioni
        FOREIGN KEY (id_competizione) REFERENCES competizioni (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT,
    CONSTRAINT fk_comp_stag_squadre_stagioni
        FOREIGN KEY (id_stagione) REFERENCES stagioni (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT,
    CONSTRAINT fk_comp_stag_squadre_squadre
        FOREIGN KEY (id_squadra) REFERENCES squadre (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
);
