# Note di analisi del progetto

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

- Il client invia i dati della grafica agli endpoint di anteprima; la risposta include l'immagine e, per Risultato, il percorso dello sfondo scelto. Il client conserva quel percorso per mantenere la stessa foto tra aggiornamenti, copia e download.
- Il download singolo e la copia usano l'anteprima corrente. Modifiche ai dati dei marcatori devono aggiornare nomi e punteggio senza cambiare arbitrariamente lo sfondo.
- L'inserimento rapido dei marcatori ACCV usa il nome completo del calciatore. Il generator può usare questo nome per trovare i dati aggiuntivi della rosa; per gli avversari il comportamento predefinito è mostrare il cognome.
- La rosa è persistita in `data/players.json`. Le modifiche al suo schema vanno propagate sia nel salvataggio API sia nella configurazione restituita al client.
- Il modulo attivato dalla matita è il modal calciatore; verificare che i campi della rosa siano all'interno del relativo form e legati a `editingPlayer` (non al modal squadra).
- Il renderer seleziona l'icona cartellino in `MatchResultGenerator._draw_card_icon`; distinguere giallo e rosso tramite il canale verde rispetto al blu, dato che entrambi hanno il rosso come canale dominante.
- `MatchResultGenerator._collect_detail_rows()` abbina gol e cartellini usando il nome completo e il cognome, così ogni giocatore occupa una sola riga nel riepilogo.
- La selezione delle emozioni è nel componente `BackgroundSelector`: il tab deve restare attivo in base alla modalità scelta, senza dipendere dal percorso dello sfondo casuale ricevuto dall'anteprima. Scegliere una categoria azzera il percorso fissato per consentire un nuovo campionamento.
- Le richieste di anteprima sono debounced; quando una modifica richiede un nuovo sfondo, ignorare le risposte obsolete che potrebbero rifissare il percorso della richiesta precedente.
- Nel tab Risultato il selettore Competizione/Stagione comprende Giornata, Data e Ora; il form usa `type="date"` e `type="time"` e converte le date italiane legacy per valorizzare il campo data.
- Le sezioni del form Risultato usano `CollapsibleSection`; la preview è esterna al form e resta sempre visibile.
- I font sono caricati da `assets/fonts/` attraverso `src/template_accv/utils/fonts.py`; verificare che la famiglia usata esista localmente perché il fallback può avere dimensioni diverse.
- Prima di modificare file, controllare `git status`: la cartella può già contenere modifiche locali dell'utente. Preservarle.

## Diario delle ricognizioni

### 2026-10-08 — Interfaccia e grafica Risultato

- Esaminati il flusso React di anteprima/download, gli endpoint server, il render Match Result, la selezione degli sfondi e la gestione della rosa.
- Aggiunta la gestione facoltativa di `display_name` alla rosa e il suo uso nel render dei marcatori ACCV; consultare il codice corrente per i dettagli implementativi.
- Trovate modifiche locali già presenti in `CHANGELOG_LAVORI.md`, `src/template_accv/models.py`, `src/template_accv/server/server.py`, `src/template_accv/generators/match_result.py` e `web/app.jsx`; mantenere prudenza prima di riorganizzare o ripristinare questi file.
