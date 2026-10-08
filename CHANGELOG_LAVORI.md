# Storico dei lavori

Questo documento registra le modifiche funzionali e tecniche apportate al progetto, così da mantenere un riferimento aggiornato per gli interventi futuri.

**Regola di manutenzione:** al termine di ogni lavoro sul progetto, aggiungere qui una nuova voce con la data, una sintesi delle modifiche e gli eventuali controlli effettuati. Il [README](README.md) rimanda a questo storico.

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
