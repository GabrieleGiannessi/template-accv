# Storico dei lavori

Questo documento registra le modifiche funzionali e tecniche apportate al progetto, così da mantenere un riferimento aggiornato per gli interventi futuri.

**Regola di manutenzione:** al termine di ogni lavoro sul progetto, aggiungere qui una nuova voce con la data, una sintesi delle modifiche e gli eventuali controlli effettuati. Il [README](README.md) rimanda a questo storico.

## 2026-11-25 — Convenzione di naming per jOOQ

- Rinominato il campo `DSLContext` da `dsl` a `create` in tutte le implementazioni DAO e registrata la convenzione nelle note di analisi.

## 2026-11-25 — Iniezione Spring e mapping note stagione

- Sistemati i frammenti di codice nei DAO, Service e controller `Resource`, mantenendo l'iniezione Spring con `@Autowired`.
- Aggiunto il mapping bidirezionale tra `StagioneDto.notes` e `Stagione.note`.
- Verifica: compilati i sorgenti backend e jOOQ generati con Java 17, senza errori o warning. La compilazione Maven è stata bloccata da un lock non apribile nella cache locale Maven.

## 2026-11-25 — Avvio backend e banner

- Aggiunto `ServletInitializer` per deployment Servlet esterno e `banner.txt` per identificare il server all'avvio.
- Configurate in chiaro in `application.yml` le credenziali MySQL `root` / `ADMIN`, allineate alla configurazione di generazione jOOQ.
- Verifica: compilati i sorgenti backend e jOOQ generati con Java 17, senza errori o warning.

## 2026-11-22 — Avvio del frontend Angular desktop

- Creata `Frontend/` come applicazione Angular standalone con Angular Material, proxy di sviluppo per Spring Boot e tema chiaro verde/grigio.
- Ricostruita la struttura della schermata desktop esistente: intestazione e navigazione a sezioni, editor sulla colonna sinistra e anteprima persistente con formati e download sulla destra.
- Collegati configurazione, anteprime, PNG, ZIP e caricamento sfondi agli endpoint REST del backend. Risultato include punteggio, marcatori, cartellini, sfondo e ritaglio; l'anteprima offre formati, copia, condivisione, area sicura e download.
- Gestione consente creazione, modifica ed eliminazione di giocatori, squadre, stagioni e competizioni, comprese le associazioni stagione-squadre. Include campi per foto e loghi.
- Inclusi adattamenti CSS per schermi stretti, senza completare il flusso mobile dedicato, previsto in una fase successiva. Restano da rifinire alcuni dettagli di parità per le sezioni Prossima Partita, MVP e Figurina.
- Installate le dipendenze con `pnpm` e generato `pnpm-lock.yaml`. La build di produzione `ng build` è riuscita; la build evita di scaricare font Google durante l'ottimizzazione. La prima build ha individuato e permesso di correggere due tipi ristretti nel template.

## 2026-11-22 — Download grafici e gestione asset nel backend Java

- Aggiunti `POST /api/download` e `/api/download-all` per Result, Next, MVP e Figurina. Il singolo restituisce PNG; il pacchetto ZIP genera i cinque rapporti disponibili oppure i tre formati Figurina usando gli stessi renderer Java delle anteprime.
- Aggiunto il codec ImageIO WebP e incluso `.webp` nella selezione degli sfondi.
- Aggiunti `GET /api/assets`, `POST /api/upload` e il servizio file `GET /assets/{path}`. Il catalogo restituisce sfondi/emozioni; l'upload accetta immagini PNG/JPEG/WebP fino a 20 MB, limita il tipo alle cartelle background/player/logo e respinge percorsi e file non validi. I loghi `competition_*` vengono copiati anche nella directory configurata delle competizioni.
- Aggiunto `GET /api/config` con squadre, giocatori, stagioni, competizioni, catalogo asset, formati e dati esempio, per mantenere il contratto di configurazione del client.
- Confrontate manualmente tutte le 18 combinazioni Pillow/Java dei formati social e Figurina: dimensioni uguali in ogni coppia. RMS RGB per canale: Result 26–37, Next 15–22, MVP 12–29, Figurina 7–16; la differenza visiva residua è coerente con rasterizzazione testo/forme e compositing.
- Verifica: `backend\\mvnw.cmd -DskipTests compile` riuscito con Java 17 e code generation jOOQ; restano i due warning MapStruct preesistenti su `StagioneMapper`. Test automatici non eseguiti.

## 2026-11-22 — Porting dei generatori grafici a Java2D

- Aggiunti i renderer Java2D per Prossima Partita, Risultato e Figurina e le anteprime `POST /api/preview/next`, `/api/preview/result` e `/api/preview/figurina`. Le risposte mantengono il formato PNG in data URI usato dal client; la figurina supporta card, story e post.
- Estratta la gestione condivisa degli sfondi; MVP usa ora la stessa routine di caricamento, contrasto, zoom/ritaglio e overlay. Risultato mantiene il proprio trattamento foto e riceve dal catalogo Spring competizione, stagione e logo quando il payload passa le relative chiavi.
- Eseguito confronto manuale Pillow/Java2D su un esempio per ogni grafica. Dimensioni corrispondenti: MVP e Next 1080×1350, Result e Figurina Story 1080×1920. RMS RGBA (R/G/B/A): MVP 28.24/24.39/17.15/4.07; Next 20.63/21.94/21.43/7.16; Result 29.01/27.22/27.72/1.32; Figurina 15.71/15.44/14.82/0. Gli scarti residui derivano soprattutto dai filtri di ridimensionamento, dall'antialias e dalle differenze di compositing Java2D/FreeType.
- Compilazione Maven Java 17 riuscita. Restano due warning MapStruct preesistenti in `StagioneMapper`; suite test non eseguita. Il supporto WebP richiede ancora un codec Image I/O.

## 2026-11-22 — Primo porting del renderer grafico Java

- Introdotta la base di rendering Java2D condivisa (`GraphicCanvas`) con testo antialias, caricamento dei font locali e disegno di pannelli arrotondati.
- Portata la grafica MVP dal generatore Pillow a `MvpGraphicRenderer`, includendo i formati social, sfondo con overlay/contrasto e risposta PNG in memoria.
- Aggiunto `POST /api/preview/mvp` con lo stesso formato JSON usato dall'anteprima esistente (`status`, `preview`, `width`, `height`, `format`). Il percorso è predisposto tramite `GRAPHICS_ASSETS_PATH`.
- Allineati i font aggiungendo Montserrat Bold, Regular e Light con licenza OFL; aggiornati gli URL di recupero al commit sorgente verificato. Allineati inoltre l'alpha dei pannelli e il sollevamento della luminosità degli sfondi scuri.
- Nuova validazione sullo stesso payload e sfondo (`4:5`, 1080×1350): compilazione Maven riuscita. I PNG risultano visivamente molto più vicini, ma non pixel-identici; RMS RGBA residuo circa 28/24/17/4 per canale, concentrato in gran parte nei contorni del testo e delle forme, dato il diverso rasterizzatore FreeType/Java2D. Suite di test non eseguita.
- La compilazione segnala due warning MapStruct preesistenti sui campi `note`/`notes` di `StagioneMapper`. Gli input WebP richiedono ancora un codec Image I/O aggiuntivo.

## 2026-11-22 — API associazioni competizione, stagione e squadra

- Aggiunto il DTO compatibile con il formato esistente `seasons: [{ season_key, team_keys }]`.
- I payload CRUD delle competizioni accettano e restituiscono le associazioni; quando il campo viene omesso, le associazioni esistenti non vengono modificate.
- Aggiunti `GET` e `PUT /api/competitions/{id}/associations`; il PUT sostituisce l'insieme delle associazioni dopo aver verificato i codici di stagione e squadra.
- Le associazioni vengono lette e scritte sulla tabella ponte esistente. Il formato legacy non può rappresentare un'associazione a stagione senza squadre, dato il modello relazionale a triple.
- Verifica: `mvnw -DskipTests compile` completato con successo, incluso il code generation jOOQ; test automatici non eseguiti. Restano warning MapStruct preesistenti sui campi note di `StagioneMapper`.

## 2026-11-22 — CRUD API Squadre separato per operazione

- Allineate le interfacce `SquadraDao` e `SquadraService` ai metodi `getSquadraById`, `selectAll`, `addSquadra`, `updateSquadra` e `deleteSquadra`; aggiunto anche il recupero/cancellazione per codice per compatibilità con il flusso esistente.
- Separati inserimento e aggiornamento: l'aggiunta non esegue più un upsert; l'aggiornamento usa l'ID e restituisce assenza se la squadra non esiste.
- Estese le rotte `/api/teams`: GET singolo, POST inserimento, PUT aggiornamento e DELETE per ID. Mantenuta la cancellazione legacy tramite codice e la risposta della lista indicizzata per codice.
- Aggiunto l'ID al DTO restituito dalle API. Controllo statico dei riferimenti e `git diff --check` eseguiti; build e test non eseguiti.
- Adeguati i ritorni di lettura e aggiornamento su richiesta: DAO e Service restituiscono direttamente modello/DTO, usando `null` quando il record non esiste invece di `Optional`.
- Stabilito il metodo privato `getInstance(<Record jOOQ>)` nei DAO per convertire record in Model tramite il mapper specifico; ordinamento standard delle classi DAO/Service `*Impl` con helper privati prima dei metodi pubblici.

## 2026-11-22 — CRUD API Giocatori

- Aggiunti Model, DTO e mapper MapStruct per i giocatori, includendo codice, nome mostrato, ruolo, numero, foto e associazione alla squadra tramite `team_id`.
- Implementate interfacce e classi `GiocatoreDao`/`GiocatoreService` con `getGiocatoreById`, `selectAll`, `addGiocatore`, `updateGiocatore` e `deleteGiocatore`; DAO usa `getInstance(GiocatoriRecord)` per le conversioni DB → Model.
- Aggiunte le rotte REST GET elenco/singolo, POST, PUT e DELETE sotto `/api/players`. Le assenze restituiscono `null` internamente e HTTP 404 all'esterno; gli errori di chiave esterna/codice duplicato restituiscono HTTP 409.
- Controllo statico dei riferimenti e `git diff --check` eseguiti; build e test non eseguiti.
- Adeguati i DAO Squadre e Giocatori: `add` e `update` ricevono ora il rispettivo DTO, convertito dal DAO tramite il mapper DTO prima del mapper record database.

## 2026-11-22 — CRUD API Stagioni

- Aggiunti Model, DTO, mapper MapStruct, DAO e Service per le stagioni, mantenendo i codici come chiavi naturali e restituendo `selectAll()` indicizzato per codice.
- Aggiunte le rotte `/api/seasons` per elenco, lettura per ID, inserimento, aggiornamento e cancellazione. Inserimento e aggiornamento DAO ricevono `StagioneDto`; il DAO applica la conversione DTO → Model → record jOOQ e usa `getInstance(StagioniRecord)` per le letture.
- Controllo statico dei riferimenti e `git diff --check` eseguiti; build e test non eseguiti.

## 2026-11-22 — CRUD API Competizioni e directory risorse

- Aggiunti Model, DTO, mapper MapStruct, DAO e Service CRUD per le competizioni; inserimento e aggiornamento DAO ricevono `CompetizioneDto`.
- Aggiunto il REST controller `/api/competitions` con elenco, lettura per ID, inserimento, aggiornamento e cancellazione. Il nome del logo accetta solo un nome file, senza percorsi.
- Configurati `resource-path` (default `C:/webdata/squadtemplate/`) e `competizioni-loghi` (default `competizioni-loghi`) in `application.yml`. All'avvio, `ResourcePathInitializer` crea la cartella radice e `CompetizioneServiceImpl` crea la sottocartella dei loghi.
- Controllo statico dei riferimenti e `git diff --check` eseguiti; build e test non eseguiti.

## 2026-11-22 — Parametri connessione MySQL in application.yml

- Esplicitato il driver JDBC e resi configurabili host, porta e nome database, mantenendo la possibilità di fornire un URL JDBC completo tramite `DB_URL`.
- Username e password restano esterni al repository tramite `DB_USERNAME` e `DB_PASSWORD`.

## 2026-11-21 — Rigenerazione classi jOOQ

- Rigenerate dal database locale `squadtemplate` le classi jOOQ con Java 17 e jOOQ 3.19.40; generate 20 classi Java in `backend/target/generated-sources/jooqdomain`.
- Credenziali passate solo all'ambiente del processo Maven e non salvate nel POM.
- Verifica: generazione Maven completata con `BUILD SUCCESS`; le classi generate restano escluse da Git tramite `backend/target/`.

## 2026-11-21 — Riferimento alle classi jOOQ in Eclipse

- Aggiunta al classpath locale Eclipse la cartella `target/generated-sources/jooqdomain`, assente dopo la rigenerazione, così l'IDE può risolvere le classi generate e i tipi `org.jooq` dal classpath Maven.
- Rimosso dal POM `addCompileSourceRoot`, parametro non riconosciuto dal plugin jOOQ 3.19.40; il code generator aggiunge già l'output tra le sorgenti di compilazione quando viene eseguito.
- Attivato il profilo Maven `jooq-codegen` per impostazione predefinita e configurata la connessione al database locale nel POM, così l'installazione da IDE genera le classi prima di compilarle.
- Verificati con successo `compile` e `install -DskipTests` con code generation automatica; l'installazione locale ha completato anche la creazione del JAR. Test non eseguiti.
- Aggiornato il progetto Maven in Spring Tools: `target/generated-sources/jooqdomain` è nuovamente visibile e la vista Problems non segnala errori (restano 2 avvisi).

## 2026-11-21 — Esclusioni IDE Java in `.gitignore`

- Esclusi i metadati locali Eclipse/Spring Tools del modulo backend (`.settings/`, `.classpath`, `.factorypath` e `.project`), le dipendenze/cache locali Angular e i file `.env` (con eccezione per `.env.example`), mantenendo versionato il Maven Wrapper e il codice sorgente.
- Controlli automatici non eseguiti.

## 2026-11-21 — Aggiornamento Spring Boot e rendering Java

- Aggiornata la base backend a Spring Boot 4.2.0, release stabile compatibile con Java 17, mantenendo le annotazioni `@Component`, `@Service` e `@RestController` già in uso.
- Allineati gli starter Maven per Spring MVC e Flyway.
- Chiarito nel piano di migrazione che Spring Boot gestisce il backend, mentre il rendering convertito da Pillow userà Java2D/Image I/O; per gli asset WebP sarà necessario aggiungere un codec.
- Verifiche automatiche non eseguite.

## 2026-11-21 — Convenzioni DAO/Service e setup Lombok IDE

- Documentata la struttura con interfacce DAO e Service separate dalle implementazioni nei package `dao.impl` e `service.impl`; i REST Controller dipendono dalle interfacce Service.
- Annotato che le classi concrete Squadre già implementate dovranno essere riallineate alla convenzione in una successiva estensione/rifattorizzazione.
- Aggiunte le istruzioni per installare Lombok nell'IDE Spring/Eclipse e riavviarlo completamente; la dipendenza Maven resta configurata nel backend.
- Controlli automatici non eseguiti.

## 2026-11-21 — Configurazione MapStruct

- Aggiunta la dipendenza MapStruct 1.6.3 e configurati i processori MapStruct, Lombok e `lombok-mapstruct-binding` per generare i mapper in compilazione.
- Abilitata l'attivazione APT di m2e e documentato il mapping tra record database, Model e DTO tramite mapper Spring generati.
- Compilazione non eseguita.

## 2026-11-21 — Refactoring del flusso Squadre

- Separati i DAO e il Service in interfacce e implementazioni nei package concordati; il REST Controller ora riceve l'interfaccia `SquadraService`.
- Aggiunti mapper MapStruct generati per la conversione da `SquadreRecord` a `Squadra` e tra `Squadra` e `SquadraDto`, mantenendo le conversioni RGB/esadecimali, i valori predefiniti e la regola specifica ACCV.
- Verificati con JDK 17 la generazione MapStruct insieme a Lombok e la compilazione di DAO, Service e REST Controller contro jOOQ 3.19.35; test non eseguiti.

## 2026-11-21 — Compatibilità jOOQ con Java 17

- Impostato jOOQ 3.19.40 per evitare l'incompatibilità del default 3.21 con il JDK 17 del progetto.
- Controllata la compilazione dei sorgenti jOOQ generati contro jOOQ 3.19.35 con Java 17. Il build Maven completo con jOOQ 3.19.40 non è stato eseguito.

## 2026-11-21 — Mapper generici e package per livello

- Introdotti i contratti `EntityDtoMapper<EntityDto, Model>` e `EntityModelDbMapper<EntityDb, Model>` con conversioni bidirezionali e collocati i mapper specifici in `dao.mapper` e `service.mapper`.
- Applicata la convenzione `<Contesto>DbMapper` a `SquadraDbMapper`; il mapper converte tra `SquadreRecord` e `Squadra`, mentre `SquadraMapper` converte tra DTO e Model.
- Aggiornato il DAO per usare il DbMapper anche per il salvataggio dei record.
- Generati e compilati con `javac` JDK 17 tutti i sorgenti backend contro jOOQ 3.19.35 dopo la rinomina; il build Maven con la versione configurata 3.19.40 non è stato eseguito. Test non eseguiti.

## 2026-11-21 — Package dei modelli e sorgenti jOOQ

- Spostati i modelli applicativi in `dao.model` e i DTO in `service.dto`.
- Configurato tutto il codice jOOQ generato sotto `target/generated-sources/jooqdomain`, registrando la cartella come sorgente Maven e legando il codegen alla fase `generate-sources`.
- Compilati con `javac` JDK 17 tutti i sorgenti applicativi e jOOQ, inclusi quelli in `target/generated-sources/jooqdomain`. Test non eseguiti; generazione Maven dal database non eseguita.

## 2026-11-12 — Marcatori distinti e interfaccia uniforme

- Corretto il raggruppamento dei marcatori: ora usa il nome completo, così giocatori con lo stesso cognome restano separati. Quando le etichette coincidono, la grafica aggiunge l'iniziale del nome (per esempio `Citti F.` e `Citti S.`); cartellini e gol confluiscono sulla riga del giocatore corretto.
- Attivato di default **Aggiungi informazioni**, anche per richieste API che non indicano il flag.
- Rimossi dai moduli e dalle grafiche i riferimenti a giornata, data e orario; il campo della prossima partita resta opzionale e la dicitura finale è stata resa neutra.
- Uniformate le testate delle sezioni principali e migliorati leggibilità, navigazione da tastiera e uso su schermi piccoli.
- Controlli: revisione statica delle modifiche; test automatici non eseguiti.

## 2026-10-08 — Marcatori e cartellini sulla stessa riga

- Accorpati per giocatore i marcatori e i cartellini gialli/rossi, mantenendo sulla riga nome, palloni e tessere.
- Ridotte di circa il 12% le icone dei cartellini nel riepilogo.
- Controllo effettuato: `git diff --check`; non sono stati eseguiti test automatici.

## 2026-10-08 — Migliorie al form Risultato

- Riattivata la selezione per emozione: la scheda resta selezionata e scegliendo o riaprendo un tema si genera una nuova copertina della categoria.
- Ignorate le risposte di anteprima superate, così un render precedente non può sostituire il tema appena scelto.
- Spostati Giornata, Data e Ora sotto Competizione e Stagione; Data e Ora ora usano campi dedicati.
- Rimosso il campo MVP dal tab Risultato e rese richiudibili tutte le sezioni del form, mantenendo l'anteprima sempre visibile.
- Controllo effettuato: `git diff --check`; non sono stati eseguiti test automatici.

## 2026-10-08 — Correzione Nome Mostrato e icona cartellino rosso

- Spostato il campo **Nome Mostrato** nel modulo di modifica/inserimento calciatore, così la matita consente di modificarlo e salvarlo.
- Corretto il criterio che distingueva i colori delle icone: il rosso non viene più scambiato per giallo.
- Controllo effettuato: `git diff --check`; non sono stati eseguiti test automatici.

## 2026-10-08 — Note permanenti per le analisi

- Creato `NOTE_ANALISI_PROGETTO.md` con la mappa delle aree esplorate, i comportamenti rilevanti e la regola di consultazione/aggiornamento per le prossime analisi.

## 2026-10-08 — Nome mostrato in rosa e riepilogo marcatori

- Aggiunto il campo facoltativo **Nome Mostrato** alla gestione dei calciatori ACCV, visibile nella tabella e nel modulo di inserimento/modifica.
- Usato il nome mostrato nella grafica Risultato per i marcatori ACCV quando compilato, mantenendo il cognome come ripiego.
- Ridotta la dimensione dei nomi dei marcatori sotto quella dei titoli squadra e aumentata la palla alla dimensione del titolo, centrata verticalmente rispetto al testo.
- Controllo effettuato: `git diff --check`; non sono stati eseguiti test automatici.

## 2026-10-08 — Icone personalizzate per i cartellini

- Scontornate le icone allegate dei cartellini gialli e rossi, preservando i dettagli interni.
- Sostituito il disegno generico dei cartellini nel riepilogo della grafica Risultato con le nuove icone.
- Controllo effettuato: `git diff --check`; non sono stati eseguiti test automatici.

## 2026-10-08 — Grafica Risultato: pallone e immagine stabile

- Sostituita l'icona disegnata del gol con il pallone allegato, scontornato in PNG trasparente.
- Scontornato il solo sfondo bianco connesso ai bordi, mantenendo il bianco nelle aree interne del pallone.
- Impostato il font dei cognomi sul carattere dei titoli e aumentata la dimensione, adattandola allo spazio disponibile.
- Fissato lo sfondo casuale scelto dall'anteprima per riutilizzarlo nei render successivi e applicato il download direttamente dall'anteprima corrente.
- La variazione automatica della categoria emotiva con il punteggio non cambia più lo sfondo mentre si aggiungono i marcatori.
- Controllo effettuato: revisione delle modifiche; non sono stati eseguiti test automatici.

## 2026-10-08 — Ingrandimento dei marcatori nella grafica Risultato

- Aumentata ulteriormente la dimensione dei cognomi e delle icone dei gol nella grafica.
- Ridisegnata l'icona del pallone con pannelli pentagonali e resa adattiva per evitare che copra il testo.
- Controllo effettuato: `git diff --check`, senza errori. Non sono stati eseguiti test automatici.

## 2026-10-08 — Rifinitura dei nomi nella grafica Risultato

- Aumentata sensibilmente la dimensione dei cognomi nella sezione marcatori/cartellini.
- Rimossi i nomi squadra e le intestazioni dalla sezione, lasciando solo cognomi e icone; il testo si adatta allo spazio disponibile.
- Controllo effettuato: `git diff --check`, senza errori. Non sono stati eseguiti test automatici.

## 2026-10-08 — Marcatori e cartellini nel risultato partita

- Collegato l'inserimento e la rimozione dei marcatori all'aggiornamento del punteggio nel form, mantenendo il supporto ai dati senza liste marcatori.
- Aggiunta la gestione dei cartellini gialli e rossi per entrambe le squadre.
- Aggiunto nella grafica Risultato il riepilogo condizionale dei marcatori con icone pallone e dei cartellini con le rispettive icone, mantenendo invariato il layout quando i dati non sono specificati.
- Controllo effettuato: `git diff --check`, senza errori. Non sono stati eseguiti test automatici.

## 2026-10-08 — Aggiornamento percorsi degli sfondi

- Adeguata la selezione casuale agli sfondi standard in `assets/backgrounds/standard/` e alle categorie in `assets/backgrounds/temi/`.
- Collegata la categoria “foto squadra” alla cartella `assets/backgrounds/foto_di_gruppo/`.
- Aggiornata la galleria server per rilevare e classificare le nuove cartelle.
- Aggiornata la documentazione dei percorsi.
- Controllo effettuato: revisione delle modifiche; non sono stati eseguiti test automatici.

## 2026-10-08 — Gestione squadre, stagioni e competizioni

- Rinominata in **Squadre** la sezione prima chiamata “Squadre Campionato”.
- Aggiunto il tab **Stagioni**, con elenco e funzioni di inserimento, modifica ed eliminazione. Ogni stagione ha codice, descrizione e note facoltative.
- Aggiunto il tab **Competizioni**, con elenco, logo e funzioni di inserimento, modifica ed eliminazione. Ogni competizione ha codice, descrizione e note facoltative.
- Aggiunte le associazioni tra competizioni e stagioni, con selezione delle squadre partecipanti per ciascuna stagione. La squadra ACCV è inclusa tra quelle selezionabili.
- Aggiunti gli endpoint server e i file dati `data/seasons.json` e `data/competitions.json`, inizializzati vuoti.
- Controllo effettuato: `git diff --check`, senza errori. Non sono stati eseguiti test automatici.

## 2026-10-08 — Selezione competizione e stagione nelle grafiche partita

- Riordinati i form **Risultato** e **Prossima Partita**: competizione e stagione sono i primi campi richiesti.
- Il selettore stagione mostra solo le stagioni associate alla competizione scelta.
- I selettori Squadra Casa e Squadra Ospite mostrano solo le squadre associate alla competizione e stagione selezionate; al cambio di selezione, le squadre non più valide vengono sostituite con squadre ammesse.
- La descrizione della competizione selezionata continua a essere usata come testo della competizione nella grafica generata.
- Controllo effettuato: `git diff --check`, senza errori. Non sono stati eseguiti test automatici.
