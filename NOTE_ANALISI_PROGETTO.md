# Note di analisi del progetto

### 2026-11-22 — Nuovo frontend Angular

- Il client Angular vive in `Frontend/`, usa Angular standalone e Angular Material; in sviluppo inoltra `/api` e `/assets` al backend locale `localhost:8080`.
- La schermata mantiene la struttura desktop del client React: navigazione superiore, form a sinistra e anteprima persistente con opzioni di esportazione a destra. I generatori iniziali e i download usano il contratto Spring esistente.
- La tab Gestione supporta CRUD per giocatori, squadre, stagioni e competizioni, comprese le associazioni di stagione e squadre per competizione. Il Risultato ha marcatori, cartellini e controlli sfondo/ritaglio; altre rifiniture delle grafiche e il layout mobile dedicato seguono in una fase successiva.

Questo documento raccoglie le informazioni emerse esplorando il codice, per evitare di ripetere ogni volta la ricognizione iniziale.

## Regola per le analisi future

Prima di esplorare il progetto per una nuova modifica o analisi, consultare queste note. Dopo aver esaminato nuove aree o aver scoperto comportamenti rilevanti, aggiungere o correggere qui le informazioni con una voce datata. Verificare sempre il codice corrente quando una nota potrebbe essere superata.

## Mappa del progetto

- `web/app.jsx`: interfaccia React. Contiene lo stato e l'anteprima delle grafiche, i form Risultato/Prossima Partita/MVP/Figurina e la gestione della rosa nella sezione **Mia Squadra**.
- `src/template_accv/server/server.py`: server HTTP e API. Costruisce i generatori dai payload, serve la configurazione iniziale e gestisce le operazioni CRUD della rosa.
- `src/template_accv/generators/match_result.py`: render della grafica Risultato; disegna punteggio, squadre, marcatori e cartellini.
- `src/template_accv/generators/base.py`: inizializza tela e sfondo; i generatori possono renderizzare e serializzare l'immagine.
- `src/template_accv/utils/backgrounds.py`: seleziona gli sfondi per categoria o casualmente e applica filtri.
- `src/template_accv/generators/figurina.py`: contiene `load_roster()`, che legge la rosa da `data/players.json`.
- `assets/icons/`: icone scontornate usate dal riepilogo Risultato (pallone, cartellino giallo e cartellino rosso).
- `CHANGELOG_LAVORI.md`: storico delle modifiche al progetto; aggiornarlo a fine intervento.

## Comportamenti da tenere presenti

- **Convenzione DAO jOOQ:** in tutte le implementazioni `dao.impl`, il campo iniettato di tipo `DSLContext` si chiama `create`.
- DAO, Service e controller `Resource` sono bean Spring e ricevono le dipendenze sui campi tramite `@Autowired`.
- L'applicazione parte da `SquadTemplateApplication`; `ServletInitializer` abilita l'integrazione con un container Servlet esterno, mentre `src/main/resources/banner.txt` definisce il banner console.
- Per l'avvio runtime servono un MySQL raggiungibile e le credenziali in chiaro configurate in `application.yml`; Flyway applica le migrazioni all'avvio. La generazione jOOQ Maven è attiva di default e richiede un database/schema già raggiungibile durante la fase `generate-sources`.
- Il flusso Squadre usa `dao.SquadraDao` con implementazione `dao.impl.SquadraDaoImpl` e `service.SquadraService` con implementazione `service.impl.SquadraServiceImpl`; `SquadreRestController` dipende dall'interfaccia Service. Questo è il riferimento per i prossimi DAO e Service.
- **Convenzione DAO `*Impl`:** definire un metodo privato `getInstance(<Record jOOQ> record)` che converte il record DB nel Model tramite il mapper DB specifico; usare questo metodo nelle query che restituiscono record. Tenere i metodi privati prima dei metodi pubblici in tutte le classi `dao.impl` e `service.impl` (campi, helper privati, costruttore e API pubbliche).
- I metodi DAO `add<Entità>` e `update<Entità>` ricevono il DTO del Service; il DAO lo converte in Model applicativo tramite il mapper DTO specifico e poi in record jOOQ tramite il mapper DB. Il Service valida/prepara il DTO prima di delegare; l'ID viene impostato dal Service per gli aggiornamenti e trasferito dal DAO al Model.
- Le API CRUD migrate dei giocatori sono nel tema `/api/players`; `GiocatoreDto.team_id` riferisce la squadra tramite ID database, mentre la risposta di `selectAll()` mantiene una mappa indicizzata per codice giocatore.
- Le API CRUD migrate delle stagioni sono nel tema `/api/seasons`; `selectAll()` mantiene una mappa indicizzata per codice stagione, come i JSON di configurazione esistenti.
- Le API CRUD migrate delle competizioni sono nel tema `/api/competitions`. `resource-path` e `competizioni-loghi` sono configurate in `backend/src/main/resources/application.yml`; `ResourcePathInitializer` crea la cartella radice e `CompetizioneServiceImpl` la sottocartella dei loghi all'avvio. Il DTO memorizza solo il nome del file logo, non un percorso arbitrario.
- Il porting Java delle grafiche è iniziato nel package `rendering`: `GraphicCanvas` fornisce primitive Java2D riutilizzabili e `MvpGraphicRenderer` genera la prima anteprima. `MvpGraphicRestController` espone `POST /api/preview/mvp`; la cartella asset si configura con `GRAPHICS_ASSETS_PATH` (default `../assets` relativo alla directory di avvio).
- I renderer Java2D ora coprono tutte le grafiche: `MvpGraphicRenderer`, `NextMatchGraphicRenderer`, `MatchResultGraphicRenderer` e `FigurinaGraphicRenderer`. I controller corrispondenti espongono le anteprime sotto `/api/preview`; `GraphicBackgroundLoader` e `GraphicImageSupport` condividono caricamento sfondi, ritaglio, logo e compositing. Il controller Risultato risolve le chiavi competizione/stagione dal database e include il percorso dello sfondo selezionato nella risposta.
- Il backend Java espone `POST /api/download` e `/api/download-all` per PNG e ZIP; `/api/config` aggrega i cataloghi dal database e gli asset, `/api/assets` elenca sfondi/emozioni, `/api/upload` salva PNG/JPEG/WebP validati e `/assets/{path}` serve gli asset entro la directory configurata. La dipendenza ImageIO WebP abilita anche la lettura degli sfondi WebP.
- I PNG Pillow/Java2D di confronto sono in `backend/target/render-validation/`. Tutti i quattro tipi sono stati generati con successo per un input campione; dettagli e RMS sono nel changelog. Differenze residue: filtri Lanczos vs Java2D e antialias dei font/form. ImageIO ora legge WebP tramite `webp-imageio`.
- Le associazioni competizione-stagione-squadra usano la tabella ponte jOOQ e codici naturali nei payload. `CompetizioneDto.seasons` conserva il formato legacy (`season_key`, `team_keys`); `PUT /api/competitions/{id}/associations` sostituisce l'insieme delle associazioni e il GET omonimo lo restituisce. La tabella a triple non rappresenta stagioni associate prive di squadre.
- Lombok è dichiarato in `backend/pom.xml` e l'annotation processing Eclipse è abilitato dal progetto. L'IDE Spring/Eclipse richiede inoltre l'installazione locale di Lombok e un riavvio completo per riconoscere nell'editor i metodi generati; le istruzioni sono nel piano di migrazione.
- MapStruct 1.6.3 è configurato in `backend/pom.xml` con generazione in compilazione e integrazione APT per m2e. `mapper.EntityDtoMapper<EntityDto, Model>` definisce la conversione DTO ↔ Model e `mapper.EntityModelDbMapper<EntityDb, Model>` quella record database ↔ Model; i mapper concreti sono nei package `service.mapper` e `dao.mapper` (con nome `<Contesto>DbMapper`). Usano `@Mapper(componentModel = "spring")`; con Lombok va mantenuto anche `lombok-mapstruct-binding`.
- jOOQ è fissato alla linea 3.19 in `backend/pom.xml` per restare compatibile con Java 17; il code generator del profilo `jooq-codegen` usa la stessa proprietà `${jooq.version}`.
- I Model applicativi appartengono a `dao.model`, i DTO a `service.dto`; tutte le classi generate da jOOQ si trovano in `backend/target/generated-sources/jooqdomain` e vengono registrate come sorgenti Maven.

- Il client invia i dati della grafica agli endpoint di anteprima; la risposta include l'immagine e, per Risultato, il percorso dello sfondo scelto. Il client conserva quel percorso per mantenere la stessa foto tra aggiornamenti, copia e download.
- Il download singolo e la copia usano l'anteprima corrente. Modifiche ai dati dei marcatori devono aggiornare nomi e punteggio senza cambiare arbitrariamente lo sfondo.
- L'inserimento rapido dei marcatori ACCV usa il nome completo del calciatore. Il generator può usare questo nome per trovare i dati aggiuntivi della rosa; per gli avversari il comportamento predefinito è mostrare il cognome.
- La rosa è persistita in `data/players.json`. Le modifiche al suo schema vanno propagate sia nel salvataggio API sia nella configurazione restituita al client.
- Il modulo attivato dalla matita è il modal calciatore; verificare che i campi della rosa siano all'interno del relativo form e legati a `editingPlayer` (non al modal squadra).
- Il renderer seleziona l'icona cartellino in `MatchResultGenerator._draw_card_icon`; distinguere giallo e rosso tramite il canale verde rispetto al blu, dato che entrambi hanno il rosso come canale dominante.
- `MatchResultGenerator._collect_detail_rows()` raggruppa gol e cartellini con il nome completo normalizzato; il solo cognome non identifica un giocatore. Se due nomi mostrati coincidono, il render aggiunge l'iniziale del nome (ad esempio `Citti F.` e `Citti S.`).
- La selezione delle emozioni è nel componente `BackgroundSelector`: il tab deve restare attivo in base alla modalità scelta, senza dipendere dal percorso dello sfondo casuale ricevuto dall'anteprima. Scegliere una categoria azzera il percorso fissato per consentire un nuovo campionamento.
- Le richieste di anteprima sono debounced; quando una modifica richiede un nuovo sfondo, ignorare le risposte obsolete che potrebbero rifissare il percorso della richiesta precedente.
- Il tab Risultato ha l'opzione `include_competition_info` attiva inizialmente; il server usa `true` anche per i payload precedenti che non specificano l'opzione.
- Le grafiche e i moduli di Risultato, Prossima Partita e MVP non devono mostrare riferimenti alla giornata, data o ora; il luogo della prossima partita resta facoltativo.
- Le sezioni principali condividono la testata pagina, i contenitori e gli stati di focus per rendere più uniforme la navigazione.
- Le sezioni del form Risultato usano `CollapsibleSection`; la preview è esterna al form e resta sempre visibile.
- I font sono caricati da `assets/fonts/` attraverso `src/template_accv/utils/fonts.py`; verificare che la famiglia usata esista localmente perché il fallback può avere dimensioni diverse.
- Prima di modificare file, controllare `git status`: la cartella può già contenere modifiche locali dell'utente. Preservarle.

## Diario delle ricognizioni

### 2026-11-22 — Endpoint backend per asset e download

- Verificati i payload legacy per configurazione, upload, anteprima e ZIP. Il backend Java ora espone configurazione aggregata, catalogo asset, upload validato, file statici, download PNG e ZIP per tutti i renderer. I contratti sono annotati nel changelog e nel piano di migrazione.

### 2026-10-08 — Interfaccia e grafica Risultato

- Esaminati il flusso React di anteprima/download, gli endpoint server, il render Match Result, la selezione degli sfondi e la gestione della rosa.
- Aggiunta la gestione facoltativa di `display_name` alla rosa e il suo uso nel render dei marcatori ACCV; consultare il codice corrente per i dettagli implementativi.
- Trovate modifiche locali già presenti in `CHANGELOG_LAVORI.md`, `src/template_accv/models.py`, `src/template_accv/server/server.py`, `src/template_accv/generators/match_result.py` e `web/app.jsx`; mantenere prudenza prima di riorganizzare o ripristinare questi file.
