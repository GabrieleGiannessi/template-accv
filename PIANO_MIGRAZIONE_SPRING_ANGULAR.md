# Piano di migrazione Spring Boot, database SQL e Angular

Documento di riferimento per pianificare e seguire la migrazione di `template-accv`. Le scelte confermate sono separate dai punti ancora aperti; aggiornare questo file man mano che vengono concordati.

## Obiettivo

Trasformare lo Studio Web ACCV in un'applicazione con backend Spring Boot, database relazionale MySQL e frontend Angular, mantenendo le funzionalità e l'identità grafica esistenti.

La migrazione sarà incrementale: prima il backend e il modello dati, poi la sostituzione dell'interfaccia. La prima versione migrata dovrà puntare alla parità funzionale con lo strumento attuale; nuove funzioni gestionali sono fuori dal perimetro iniziale, salvo diversa decisione.

## Decisioni concordate

| Area | Decisione | Stato |
|---|---|---|
| Backend applicativo | Spring Boot | Confermata |
| Versione Spring Boot | 4.2.0, release stabile compatibile con Java 17 | Scelta aggiornata il 2026-11-21 |
| Modulo Maven | Cartella `backend/` | Confermata |
| Comandi Maven | Maven Wrapper (`mvnw` / `mvnw.cmd`), distribuzione Maven 3.10.1 | Configurato |
| Package Java base | `com.giannesuite.squadtemplate` | Confermata |
| Versione Java | Java 17 | Confermata, da verificare sul progetto Maven selezionato |
| Fedeltà del renderer | Resa visivamente equivalente a Pillow; non è richiesta corrispondenza pixel per pixel | Confermata |
| Database | MySQL | Confermata |
| Accesso ai dati | jOOQ per interrogazioni e operazioni SQL | Confermata; JPA/Hibernate non previsto |
| Versione jOOQ | 3.19.40 per mantenere la compatibilità con Java 17 | Fissata il 2026-11-21 |
| Classi jOOQ | Code generation dallo schema MySQL, dopo le migrazioni Flyway | Confermata |
| Generazione jOOQ | Profilo Maven `jooq-codegen`, sorgenti in `target/generated-sources/jooqdomain` | Configurata |
| Migrazioni database | Flyway | Confermata |
| Convenzione SQL | Nomi di tabelle e colonne in italiano; PK autoincrementali `id`; codici naturali `codice`; niente dichiarazioni `ENGINE`, charset o `COLLATE` | Confermata |
| Frontend | Angular | Confermata |
| Componenti UI | Angular Material e componenti gratuiti | Confermata il 2026-11-22 |
| Priorità frontend | Schermata desktop con la disposizione esistente; flusso mobile essenziale in una fase successiva | Confermata il 2026-11-22 |
| Rotte API | Inglese per mantenere il contratto attuale; rinomina successiva in italiano plurale | Confermata |
| Strati backend | Interfacce DAO e Service separate dalle implementazioni; Controller dipende dall'interfaccia Service | Confermata il 2026-11-21 |
| Mapping tra record database, Model e DTO | MapStruct con implementazioni di mapper generate in compilazione | Confermata il 2026-11-21 |
| Package dei modelli e dei DTO | Modelli in `dao.model`; DTO in `service.dto` | Confermata il 2026-11-21 |
| Sorgenti generate jOOQ | `backend/target/generated-sources/jooqdomain` | Confermata il 2026-11-21 |
| Boilerplate Java | Lombok, con uso dell'annotazione `@Data` | Confermata |
| Ordine di lavoro | Modello dati e API, backend, integrazione grafica, frontend Angular | Confermato |
| Generazione delle immagini | Portare in Java i comportamenti dei generatori Python/Pillow | Obiettivo confermato; confronto con Python durante la conversione |
| Migrazione | Procedere per fasi mantenendo come riferimento il comportamento attuale | Confermata |
| Versione attuale | Conservata su `master`; sviluppo sul branch `codex/migrazione-spring-angular` | Confermata |

## Stato attuale del progetto

- Il server è scritto in Python e usa `http.server`; espone API JSON e serve anche frontend e file statici.
- Il frontend è una singola applicazione React in `web/app.jsx`, attualmente caricata da `web/index.html` insieme a React e Babel via CDN.
- La generazione delle immagini è implementata in Python con Pillow e usa moduli distinti per Risultato, Prossima Partita, MVP e Figurine.
- Giocatori, squadre, stagioni e competizioni sono salvati in file JSON sotto `data/`.
- Le immagini, i loghi, i font e gli altri asset sono organizzati sotto `assets/`; nel checkout analizzato occupano complessivamente circa 500 MB.
- Il server espone configurazione, anteprime, download singoli e multipli, caricamento di asset e funzioni CRUD per i dati gestionali.
- La gestione degli incontri non risulta attualmente un archivio persistente: i dati della grafica vengono inviati per il rendering. Decidere separatamente se la migrazione debba introdurre uno storico partite.

### Avanzamento della migrazione — 2026-11-22

- **Squadre:** primo tema API completato con DAO/Service separati per lettura per ID, elenco, inserimento, aggiornamento e cancellazione. REST disponibile su `/api/teams`; la lista mantiene la forma indicizzata per codice e la cancellazione legacy per codice resta supportata.
- **Giocatori:** CRUD REST disponibile su `/api/players`, con associazione alla squadra tramite `team_id` e mantenimento della lista indicizzata per codice.
- **Stagioni:** CRUD REST disponibile su `/api/seasons`, con lista indicizzata per codice.
- **Competizioni:** CRUD REST disponibile su `/api/competitions`; `resource-path` e `competizioni-loghi` configurano la cartella risorse e la sottocartella loghi, create all'avvio.
- **Associazioni:** API di lettura e sostituzione disponibili su `/api/competitions/{id}/associations`; i payload CRUD delle competizioni mantengono anche la forma legacy annidata `seasons` con `season_key` e `team_keys`.
- **Altri temi:** Le API delle associazioni sono una compatibilità transitoria: in futuro ridurre il numero di chiamate e rivedere il contratto. Il frontend Angular non è ancora avviato.
- **Rendering Java:** portati in Java2D MVP, Prossima Partita, Risultato e Figurina. Anteprime disponibili su `/api/preview/mvp`, `/api/preview/next`, `/api/preview/result` e `/api/preview/figurina`; la Figurina comprende card, story e post. Montserrat è incluso con licenza OFL. `webp-imageio` abilita gli input WebP. Sono disponibili `POST /api/download` e `/api/download-all` per PNG singoli e pacchetti ZIP: cinque formati per Result/Next/MVP e tre per Figurina. Confrontate manualmente tutte le 18 combinazioni Pillow/Java: dimensioni corrispondenti e resa visivamente vicina, con scarti residui di antialias e compositing registrati nel changelog.
- **Asset e configurazione:** `GET /api/config` restituisce cataloghi e opzioni del client; `GET /api/assets` espone sfondi ed emozioni; `POST /api/upload` gestisce immagini validate fino a 20 MB e `GET /assets/{path}` le serve dalla directory configurata. L'upload dei loghi competizione aggiorna anche la directory risorse persistente.
- **Frontend Angular desktop:** creato `Frontend/` con Angular standalone e Angular Material. La schermata conserva testata/nav, editor a sinistra e anteprima/azioni a destra. Configurazione, rendering, download, upload e CRUD dei cataloghi, incluse le associazioni competizione-stagione-squadra, sono collegati. Build di produzione riuscita; restano rifiniture di parità per Prossima Partita, MVP e Figurina.
- **Prossimo passaggio:** completare le rifiniture desktop; la versione mobile dedicata, limitata alle funzioni essenziali di generazione, è successiva e verrà definita separatamente.

## Architettura iniziale proposta

1. **Angular** gestisce interfaccia, moduli, anteprima, richieste API e download.
2. **Spring Boot** espone API REST, valida le richieste e legge/scrive i dati nel database SQL.
3. **Renderer Java** produce le immagini all'interno del backend, replicando i comportamenti dei generatori Python/Pillow. Durante la conversione il codice Python e i suoi output restano il riferimento per confrontare i risultati; il renderer Python non è l'architettura finale.
4. **Database SQL** contiene i dati strutturati. Fotografie e altri file restano file o oggetti gestiti da un archivio: non si prevede di inserire i file immagine direttamente nelle tabelle.

La conversione del renderer è parte del lavoro. Si può procedere per generatori e funzioni, usando gli output Python come riferimento visivo fino alla verifica di una resa visivamente equivalente; non è richiesta corrispondenza pixel per pixel.

### Porting del rendering delle immagini

Java 17 include Java2D e Image I/O: `BufferedImage` consente di lavorare sui pixel e creare un `Graphics2D` per disegnare; `Graphics2D` supporta immagini, trasformazioni, clipping e compositing trasparente. Queste API coprono le primitive impiegate dai generatori, ma non garantiscono automaticamente risultati identici a Pillow. Spring Boot e le sue annotazioni (`@Component`, `@Service`, `@RestController`) organizzano e avviano il backend, ma non eseguono il rendering: i generatori saranno servizi Java basati su Java2D/Image I/O e su librerie aggiuntive dove servono.

Nel codice Python sono usati caricamento/conversione RGBA, ritaglio e ridimensionamento Lanczos, testo con font locali, forme e maschere, alpha compositing, overlay e sfocatura gaussiana. Il progetto accetta anche asset WebP: per questi input servirà un codec Image I/O aggiuntivo, poiché il JDK non lo legge in modo nativo. In Java questi comportamenti andranno implementati e confrontati visivamente; filtri e metriche dei font potrebbero richiedere logica dedicata o una libreria Java aggiuntiva se Java2D non offre una fedeltà sufficiente. Le immagini generate continueranno a essere esportate in PNG.

### Configurazione Spring e jOOQ

- Le dipendenze Spring Boot, il driver MySQL e l'integrazione jOOQ saranno dichiarati nel `pom.xml`.
- Spring Boot configurerà la connessione e fornirà il `DSLContext` jOOQ ai componenti applicativi.
- URL, utente e password del database saranno configurati per ambiente tramite configurazione applicativa e variabili d'ambiente/segreti di deploy; non saranno inseriti nel `pom.xml` né codificati nelle classi Java.
- Le classi Java useranno le annotazioni Spring per controller, servizi e altri componenti necessari.
- Le classi jOOQ saranno generate dallo schema MySQL dopo l'applicazione delle migrazioni Flyway.
- Tutte le classi generate da jOOQ (tabelle, record, POJO e classi correlate) risiedono in `backend/target/generated-sources/jooqdomain`; il plugin Maven registra questa cartella come sorgente di compilazione. Non modificare né versionare manualmente questi file: rigenerarli dal profilo `jooq-codegen` dopo aver aggiornato lo schema.

### Organizzazione dei componenti backend

La struttura applicativa segue il flusso REST Controller → interfaccia Service → implementazione Service → interfaccia DAO → implementazione DAO → database. Il REST Controller dipende dall'interfaccia Service; l'implementazione del Service applica le regole applicative e usa DTO in `service.dto` per lo scambio con il Controller; l'implementazione DAO incapsula le query jOOQ, converte i risultati in modelli (`dao.model`) e li restituisce al Service.

| Livello | Interfaccia | Implementazione | Responsabilità |
|---|---|---|---|
| DAO | `dao.SquadraDao`, `dao.CompetizioneDao`, ecc. | `dao.impl.SquadraDaoImpl`, `dao.impl.CompetizioneDaoImpl`, ecc. | Accesso al database con jOOQ e conversione dei record in modelli |
| Service | `service.SquadraService`, `service.CompetizioneService`, ecc. | `service.impl.SquadraServiceImpl`, `service.impl.CompetizioneServiceImpl`, ecc. | Logica applicativa e coordinamento dei DAO; uso dei DTO al confine con il Controller |
| REST Controller | Nessuna interfaccia prevista in questa convenzione | Controller annotati `@RestController` | API HTTP e delega al Service tramite la sua interfaccia |

Le interfacce dichiarano le operazioni disponibili; le classi `*Impl` contengono la logica e sono registrate come componenti Spring. Il flusso Squadre è il primo ad adottare questa struttura; DAO, Service e Controller usano `SquadraDao`, `SquadraService` e le rispettive implementazioni.

### Mapping con MapStruct

Le conversioni usano due contratti generici bidirezionali nel package comune `mapper`: `EntityDtoMapper<EntityDto, Model>` per DTO ↔ Model e `EntityModelDbMapper<EntityDb, Model>` per record/oggetto database ↔ Model. Le interfacce specifiche li estendono e sono collocate per livello: `service.mapper.SquadraMapper` per DTO ↔ Model e `dao.mapper.SquadraDbMapper` per record jOOQ ↔ Model. Per ogni contesto database si usa il nome `<Contesto>DbMapper` (per esempio `SquadraDbMapper`); il suffisso `RecordMapper` non viene usato.

Le interfacce concrete sono annotate `@Mapper(componentModel = "spring")`; MapStruct genera le implementazioni in compilazione, iniettabili nei Service e nei DAO. Le corrispondenze con nomi o formati diversi (per esempio RGB ↔ stringa esadecimale o nomi inglesi ↔ campi italiani) sono dichiarate con annotazioni MapStruct o metodi di conversione dedicati. Il DAO usa il DbMapper anche per trasformare il Model in record jOOQ durante il salvataggio.

La build Maven include MapStruct 1.6.3 e configura i processori MapStruct, Lombok e `lombok-mapstruct-binding`; quest'ultimo consente ai due processori di cooperare. L'attivazione APT di m2e è indicata nel POM per rendere disponibili i mapper generati anche durante la compilazione incrementale in Eclipse/STS. jOOQ è fissato alla linea 3.19 per mantenere Java 17: l'edizione open source delle linee più recenti richiede un JDK più nuovo.

Flyway gestirà le migrazioni versionate dello schema MySQL. Lombok sarà usato per ridurre il boilerplate, includendo `@Data` nei tipi di dati appropriati. I punti precisi di applicazione dell'annotazione saranno definiti insieme ai modelli/DTO durante l'implementazione.

### Configurazione di Lombok nell'IDE

Lombok è una dipendenza Maven del backend e l'elaborazione delle annotazioni è abilitata nelle impostazioni Eclipse del progetto. Perché Spring Tools Suite/Eclipse riconosca nell'editor i metodi generati da annotazioni come `@Data`, Lombok deve essere installato anche nell'IDE dello sviluppatore: eseguire il `lombok.jar` risolto dalla cache Maven, selezionare l'installazione STS/Eclipse e completare l'installazione. Chiudere completamente e riaprire l'IDE dopo la modifica. L'installazione è locale alla macchina e il jar non va copiato nel repository. Dopo un aggiornamento Maven del progetto, verificare nella finestra **About** che l'IDE riporti Lombok.

## Modello dati da progettare

Le entità iniziali da ricavare dai JSON sono:

- **Giocatore**: chiave, nome, nome mostrato, ruolo, numero di maglia e squadra.
- **Squadra**: chiave, nome, nome esteso, sigla, colori, note, logo e, quando applicabile, rivalità.
- **Stagione**: chiave, descrizione e note.
- **Competizione**: chiave, descrizione, note e logo.
- **Associazione competizione-stagione-squadra**: le competizioni attuali dichiarano le squadre partecipanti per stagione; la relazione va modellata esplicitamente nel database.

La strategia delle chiavi, i vincoli di unicità, la cancellazione di record collegati e l'eventuale entità Partita rimangono da concordare prima di fissare lo schema.

## Funzionalità da mantenere

- Creazione delle anteprime per Risultato, Prossima Partita, MVP e Figurina.
- Download della singola immagine e pacchetti ZIP nei formati supportati.
- Gestione di giocatori, squadre, stagioni e competizioni.
- Caricamento e consultazione di foto, loghi e sfondi.
- Selezione delle categorie di sfondo e mantenimento dello sfondo scelto durante gli aggiornamenti dell'anteprima.
- Funzioni d'uso della preview come selezione formato, ritaglio/copertina e condivisione supportata dal browser.

## Sequenza di lavoro

### Fase 1 — Requisiti backend e contratti API

- Confermare motore e versione del database.
- Definire i casi d'uso API e i formati di richiesta/risposta usati dal frontend.
- Decidere convenzioni REST, gestione errori e validazione.
- Definire il confine di integrazione con il renderer Python.

### Fase 2 — Modello relazionale e progetto Spring Boot

- Disegnare schema e relazioni.
- Creare il progetto Spring Boot e la configurazione per ambienti locale e ospitato.
  - Configurare Flyway come fonte versionata dello schema e generare da quello le classi jOOQ.
- Aggiungere un'importazione ripetibile dei JSON iniziali.

### Fase 3 — API e porting grafico Java

- Implementare API per i dati di configurazione e le operazioni CRUD.
- Portare in Java le primitive grafiche condivise e i generatori, procedendo per tipo e mantenendo gli output Python come riferimento.
- Implementare preview, download e ZIP usando il renderer Java, confrontando i risultati con le grafiche correnti.
- Gestire caricamento, validazione e recupero degli asset.

### Fase 4 — Frontend Angular

- Creare l'applicazione Angular e il client API.
- Ricostruire i flussi esistenti per sezioni, form e gestione dati.
- Ricollegare anteprima, selezione dei formati, download e caricamenti.

### Fase 5 — Distribuzione e passaggio

- Configurare backend, database, renderer e frontend per l'ambiente di destinazione.
- Scegliere una strategia persistente per gli asset caricati.
- Confrontare i flussi migrati con quelli attuali e preparare il passaggio mantenendo `master` come riferimento.

## Punti ancora da decidere

1. Versione MySQL, in base all'ambiente di deploy.
2. Modalità di esecuzione e comunicazione con il renderer Python.
3. Destinazione degli asset in produzione: filesystem persistente o object storage; regole per nomi, dimensioni e tipi di file caricabili.
4. Se includere autenticazione e ruoli nella prima versione. L'applicazione attuale non presenta un flusso di login.
5. Se aggiungere lo storico delle partite o migrare solo le funzioni esistenti.
6. Ambiente di deploy previsto e configurazione del database per tale ambiente.
7. Versioni Java, Spring Boot, Angular e Node da adottare, da definire all'avvio del progetto.

## Backup e flusso Git

- Il codice iniziale era pulito su `master` al commit `6940b62` (`WIP`).
- La migrazione procede sul branch `codex/migrazione-spring-angular`.
- `master` resta il riferimento per recuperare il progetto attuale. Nessuna copia separata della cartella è stata creata.
- Prima di ogni passaggio rilevante, controllare lo stato Git e mantenere le modifiche locali dell'utente.

## Stima indicativa iniziale

Stima preliminare per una persona con esperienza adeguata, a tempo pieno, portando il renderer in Java e puntando alla parità funzionale:

- backend, persistenza, importazione e API: circa 2–4 settimane;
- porting del renderer e gestione degli asset: circa 2–4 settimane, in parte sovrapponibili al backend;
- ricostruzione frontend Angular: circa 2–3 settimane;
- distribuzione e confronto dei flussi: circa 1–2 settimane.

Ordine di grandezza complessivo: **8–12 settimane o più**, includendo il porting dei generatori in Java 17. La stima è provvisoria e dipende dalla quantità di rifiniture necessarie per ottenere una resa visivamente equivalente, oltre che da deploy, autenticazione, archivio immagini e perimetro dello storico partite.

## Diario delle decisioni

### 2026-11-21 — Avvio della migrazione

- Confermati Spring Boot, Angular e MySQL come database.
- Concordati il modulo Maven in `backend/` e il package base `com.giannesuite.squadtemplate`.
- Scelto jOOQ per l'accesso ai dati, con componenti Spring e configurazione delle credenziali per ambiente.
- Confermato l'uso del code generation jOOQ dallo schema MySQL gestito da Flyway.
- Scelto Java 17 e confermato il porting in Java dei comportamenti grafici Pillow; gli output Python saranno usati come riferimento durante la conversione.
- Definito come criterio grafico una resa visivamente equivalente, senza vincolo di corrispondenza pixel per pixel.
- Scelto Flyway per le migrazioni MySQL, l'organizzazione DAO → Service → REST Controller con DTO nel livello Service e Lombok con `@Data`.
- Scelto di partire da modello dati e API; i generatori Python/Pillow serviranno come riferimento durante il porting in Java.
- Creato il branch `codex/migrazione-spring-angular`; il ramo `master` conserva il punto di partenza.
- Creato lo scheletro Maven iniziale in `backend/` con Spring Boot 3.5.16, Java 17, starter web/jOOQ, Flyway, driver MySQL e Lombok. La connessione è configurata tramite variabili d'ambiente; le credenziali non sono salvate nel repository.
- Approvata la struttura iniziale e collocata la migrazione `backend/src/main/resources/db/migration/V1.0.0__Init.sql` nel percorso attivo di Flyway.
- Creato il database locale `squadtemplate` sul server MySQL indicato e applicata la migrazione Flyway `1.0.0 - Init`; non sono stati importati i dati JSON.
- Configurato il profilo Maven `jooq-codegen` e generate le classi jOOQ a partire dallo schema MySQL; la compilazione del backend con Java 17 è riuscita. La rigenerazione richiede il profilo Maven e le variabili di connessione.
- Aggiunta e applicata la migrazione `V1.0.1__Dati_iniziali.sql`: importati 15 squadre, 2 stagioni, 1 competizione, 18 giocatori e 24 associazioni competizione/stagione/squadra. Sono presenti 17 foto per 18 giocatori, coerentemente con gli asset attuali.
- Flyway registra correttamente le versioni `1.0.0` e `1.0.1`; il server Spring temporaneo è stato arrestato al termine delle migrazioni.
- Implementato il primo flusso squadre con `GET /api/teams`, `POST /api/teams` e `POST /api/teams/delete`, mantenendo la forma dei payload inglesi attuali. Il flusso usa DAO, modello Lombok `@Data`, service e DTO; compilazione Java 17 riuscita, test non eseguiti.
- Confermato di mantenere temporaneamente in inglese le rotte e il contratto API attuale; in una fase successiva le rotte saranno rinominate in italiano plurale.
- Aggiunto Maven Wrapper al modulo `backend/`, fissando Maven 3.10.1; `clean compile` è riuscito con JDK 17. Comandi Windows dalla cartella `backend`: `.\mvnw.cmd clean`, `.\mvnw.cmd compile`, `.\mvnw.cmd install`.
- L'ambiente Eclipse `backend/.classpath` usa il container JDK `jdk-17.0.21`; il terminale attuale non ha `java` o `mvn` nel `PATH`.
- Per la bozza iniziale è stata richiesta la nomenclatura italiana di tabelle e colonne e la rimozione delle opzioni `ENGINE`, charset e `COLLATE` da tutte le tabelle.
- Le chiavi primarie autoincrementali si chiamano `id`; i codici testuali univoci si chiamano `codice`.
- Aggiornata la versione di Spring Boot a 4.2.0, release stabile compatibile con Java 17. La versione mantiene le annotazioni Spring già usate dal progetto; aggiornati gli starter Maven per Spring MVC e Flyway. Il rendering Pillow→Java resta indipendente dalla versione Spring: Java2D/Image I/O farà il rendering, con un codec aggiuntivo per leggere gli asset WebP.
- Confermata la separazione di DAO e Service in interfacce e implementazioni (`dao`/`dao.impl`, `service`/`service.impl`); i Controller useranno le interfacce Service. Registrata la configurazione necessaria per installare Lombok anche nell'IDE Spring/Eclipse oltre alla dipendenza Maven.
- Scelto MapStruct 1.6.3 per generare i mapper tra record/oggetti database e Model e tra Model e DTO; configurati in Maven il processore MapStruct, Lombok, `lombok-mapstruct-binding` e l'attivazione APT per m2e.
- Rifattorizzato il flusso Squadre con `SquadraDao`/`SquadraDaoImpl` e `SquadraService`/`SquadraServiceImpl`; il controller dipende dall'interfaccia Service. Aggiunti i contratti bidirezionali `EntityDtoMapper` e `EntityModelDbMapper`, con mapper specifici nei package `dao.mapper` (`SquadraDbMapper`) e `service.mapper` (`SquadraMapper`). Il salvataggio usa la mappatura Model → record jOOQ.
- Spostati i modelli applicativi in `dao.model` e i DTO in `service.dto`. Spostato tutto l'output jOOQ in `backend/target/generated-sources/jooqdomain`, registrato come sorgente di compilazione e generato nella fase Maven `generate-sources`.
- Fissato jOOQ alla versione 3.19.40 perché la versione gestita inizialmente da Spring Boot 4.2 richiede Java 21; i sorgenti jOOQ generati esistenti sono stati compilati con successo usando jOOQ 3.19.35 e Java 17.
