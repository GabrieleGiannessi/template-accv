# Storico dei lavori

Questo documento registra le modifiche funzionali e tecniche apportate al progetto, così da mantenere un riferimento aggiornato per gli interventi futuri.

**Regola di manutenzione:** al termine di ogni lavoro sul progetto, aggiungere qui una nuova voce con la data, una sintesi delle modifiche e gli eventuali controlli effettuati. Il [README](README.md) rimanda a questo storico.

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
