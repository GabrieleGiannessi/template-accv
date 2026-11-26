-- Dati iniziali convertiti dai file JSON in data/.
-- Le immagini restano in assets/: qui sono salvati solo i nomi dei file.

-- Squadre
INSERT INTO squadre (codice, nome, nome_esteso, nome_breve, colore_primario, colore_secondario, colore_terziario, note, file_logo, rivalita)
VALUES
    ('acab', 'ACAB', 'Alle Cinque al Bar', 'ACB', '#000000', '#FFFFFF', '#EDE502', '', 'acab.png', 4),
    ('accv', 'ACCV', 'AC Chiavo Veronica', 'ACCV', '#10B981', '#FFFFFF', '#D1BA23', '', 'accv.png', NULL),
    ('antraccoli', 'Antraccoli', '', 'ANT', '#07CF0B', '#EF4444', '#3B82F6', '', 'antraccoli.png', 3),
    ('arrosto_fc', 'Arrosto FC', '', 'ARR', '#FF7800', '#EF4444', '#3B82F6', '', 'arrosto_fc.png', 1),
    ('blitzball', 'Blitzball', '', 'BLI', '#8B5CF6', '#EF4444', '#3B82F6', '', 'blitzball.png', 3),
    ('hellas_peroni', 'Hellas Peroni', '', 'HPR', '#D1B31F', '#143CDB', '#FFFFFF', '', 'hellas_peroni.png', 1),
    ('imacellai', 'SPRF', '', 'SPRF', '#000000', '#827521', '#FFFFFF', '', 'imacellai.png', 2),
    ('jagerbombers', 'Jagerbombers', '', 'JAG', '#10B981', '#D6E316', '#FFFFFF', '', 'jagerbombers.png', 3),
    ('las_furias_rosadas', 'Las Furias Rosadas', '', 'LFR', '#F372E8', '#000000', '#F066DD', '', 'las_furias_rosadas.png', 1),
    ('lucca7kicks', 'Lucca7Kicks', '', 'L7K', '#FFFFFF', '#EF4444', '#BBBEC3', '', 'lucca7kicks.png', 2),
    ('real_nozzano', 'Real Nozzano', '', 'REA', '#9333EA', '#EF4444', '#3B82F6', '', 'real_nozzano.png', 4),
    ('san_ginese', 'San Ginese', '', 'SAN', '#E01B24', '#FFFFFF', '#0D0D0D', '', 'san_ginese.png', 4),
    ('seven_samba', 'Seven Samba', '', 'SVN', '#000000', '#FFFFFF', '#000000', '', 'seven_samba.png', 3),
    ('totutti', 'Totutti', '', 'TOT', '#74962C', '#FFFFFF', '#000000', '', 'totutti.png', 4),
    ('underdogs', 'Underdogs', '', 'UND', '#87A198', '#EF4444', '#3B82F6', '', 'underdogs.png', 1);

-- Stagioni
INSERT INTO stagioni (codice, descrizione, note)
VALUES
    ('season2025-2026', '2025-2026', 'Stagione 2025/2026'),
    ('season2026-2027', '2026-2027', 'Stagione 2026/2027');

-- Competizioni
INSERT INTO competizioni (codice, descrizione, note, file_logo)
VALUES
    ('perini-carraia-b', 'Campionato Perini/Carraia B', '', 'competition_perini-carraia-b.png');

-- Giocatori; la rosa attuale appartiene alla squadra ACCV.
INSERT INTO giocatori (codice, nome, nome_mostrato, ruolo, numero_maglia, id_squadra, file_foto)
VALUES
    ('bonaventura', 'Cristian Bonaventura', '', 'Centrocampista', '27', (SELECT id FROM squadre WHERE codice = 'accv'), NULL),
    ('bouba', 'Boubacar Gadio', '', 'Centrocampista', '8', (SELECT id FROM squadre WHERE codice = 'accv'), 'bouba.png'),
    ('dario', 'Dario Stagi', '', 'Difensore', '16', (SELECT id FROM squadre WHERE codice = 'accv'), 'dario.png'),
    ('diego', 'Diego Leon', '', 'Attaccante', '76', (SELECT id FROM squadre WHERE codice = 'accv'), 'diego.png'),
    ('elia', 'Elia Zanchetta', '', 'Portiere', '1', (SELECT id FROM squadre WHERE codice = 'accv'), 'elia.png'),
    ('f-citti', 'Federico Citti', 'Citti F.', 'Centrocampista', '69', (SELECT id FROM squadre WHERE codice = 'accv'), 'f-citti.png'),
    ('fuccio', 'Lorenzo Fuccio', '', 'Centrocampista', '7', (SELECT id FROM squadre WHERE codice = 'accv'), 'fuccio.png'),
    ('g-giannessi', 'Gabriele Giannessi', 'Giannessi G.', 'Difensore', '20', (SELECT id FROM squadre WHERE codice = 'accv'), 'g-giannessi.png'),
    ('gigi', 'Luigi Madda', '', 'Allenatore', '4', (SELECT id FROM squadre WHERE codice = 'accv'), 'gigi.png'),
    ('l-giannessi', 'Lorenzo Giannessi', 'Giannessi L.', 'Difensore', '11', (SELECT id FROM squadre WHERE codice = 'accv'), 'l-giannessi.png'),
    ('laidel', 'Laidel Adonis', '', 'Attaccante', '99', (SELECT id FROM squadre WHERE codice = 'accv'), 'laidel.png'),
    ('martinelli', 'Federico Martinelli', '', 'Difensore', '2', (SELECT id FROM squadre WHERE codice = 'accv'), 'martinelli.png'),
    ('ricci', 'Leonardo Ricci', '', 'Centrocampista', '23', (SELECT id FROM squadre WHERE codice = 'accv'), 'ricci.png'),
    ('rossi', 'Matteo Rossi', '', 'Difensore', '3', (SELECT id FROM squadre WHERE codice = 'accv'), 'rossi.png'),
    ('s-citti', 'Samuele Citti', 'Citti S.', 'Attaccante', '32', (SELECT id FROM squadre WHERE codice = 'accv'), 's-citti.png'),
    ('samu-rama', 'Samuele Ramacciotti', '', 'Portiere', '12', (SELECT id FROM squadre WHERE codice = 'accv'), 'samu-rama.png'),
    ('tersitti', 'Lorenzo Tersitti', '', 'Difensore', '6', (SELECT id FROM squadre WHERE codice = 'accv'), 'tersitti.png'),
    ('tintori', 'Filippo Tintori', '', 'Centrocampista', '10', (SELECT id FROM squadre WHERE codice = 'accv'), 'tintori.png');

-- Associazioni squadra / stagione / competizione
INSERT INTO competizioni_stagioni_squadre (id_competizione, id_stagione, id_squadra)
SELECT c.id, s.id, sq.id
FROM (
    SELECT 'perini-carraia-b' AS codice_competizione, 'season2026-2027' AS codice_stagione, 'accv' AS codice_squadra
    UNION ALL SELECT 'perini-carraia-b', 'season2026-2027', 'arrosto_fc'
    UNION ALL SELECT 'perini-carraia-b', 'season2026-2027', 'hellas_peroni'
    UNION ALL SELECT 'perini-carraia-b', 'season2026-2027', 'jagerbombers'
    UNION ALL SELECT 'perini-carraia-b', 'season2026-2027', 'lucca7kicks'
    UNION ALL SELECT 'perini-carraia-b', 'season2026-2027', 'san_ginese'
    UNION ALL SELECT 'perini-carraia-b', 'season2026-2027', 'totutti'
    UNION ALL SELECT 'perini-carraia-b', 'season2026-2027', 'underdogs'
    UNION ALL SELECT 'perini-carraia-b', 'season2026-2027', 'seven_samba'
    UNION ALL SELECT 'perini-carraia-b', 'season2026-2027', 'real_nozzano'
    UNION ALL SELECT 'perini-carraia-b', 'season2026-2027', 'las_furias_rosadas'
    UNION ALL SELECT 'perini-carraia-b', 'season2026-2027', 'imacellai'
    UNION ALL SELECT 'perini-carraia-b', 'season2026-2027', 'acab'
    UNION ALL SELECT 'perini-carraia-b', 'season2025-2026', 'acab'
    UNION ALL SELECT 'perini-carraia-b', 'season2025-2026', 'antraccoli'
    UNION ALL SELECT 'perini-carraia-b', 'season2025-2026', 'blitzball'
    UNION ALL SELECT 'perini-carraia-b', 'season2025-2026', 'real_nozzano'
    UNION ALL SELECT 'perini-carraia-b', 'season2025-2026', 'seven_samba'
    UNION ALL SELECT 'perini-carraia-b', 'season2025-2026', 'totutti'
    UNION ALL SELECT 'perini-carraia-b', 'season2025-2026', 'san_ginese'
    UNION ALL SELECT 'perini-carraia-b', 'season2025-2026', 'lucca7kicks'
    UNION ALL SELECT 'perini-carraia-b', 'season2025-2026', 'jagerbombers'
    UNION ALL SELECT 'perini-carraia-b', 'season2025-2026', 'accv'
    UNION ALL SELECT 'perini-carraia-b', 'season2025-2026', 'imacellai'
) AS dati
JOIN competizioni c ON c.codice = dati.codice_competizione
JOIN stagioni s ON s.codice = dati.codice_stagione
JOIN squadre sq ON sq.codice = dati.codice_squadra;
