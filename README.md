# ACCV - Calcetto Social Graphics Automation ⚽

Automazione per la creazione di template grafici social (Instagram Feed 1080x1080 & Instagram Stories 1080x1920) per i risultati delle partite e gli eventi della squadra di calcetto **A.C.C.V.**.

---

## 🎨 Tipologie di Grafiche Supportate

1. **Risultato Finale (`MatchResult`)**:
   - Punteggio partita in evidenza con badge al neon.
   - Stemmi delle due squadre (o badge generati automaticamente).
   - Lista marcatori divisa per squadra (con conteggio gol per ciascun giocatore).
   - Menzione del *Migliore in Campo (MVP)*.
   - Data, ora, campo e competizione.

2. **Prossima Partita (`NextMatch`)**:
   - Grafica promozionale con abbinamento squadre (VS).
   - Dettagli evento (data, ora di calcio d'inizio, impianto sportivo).

3. **Migliore in Campo (`MVP`)**:
   - Scheda giocatore in stile carte FUT / eSports con accenti dorati.
   - Numero di maglia (#10, #7, ecc.), ruolo e statistiche (gol, assist, parate, voto pagella).

---

## 📁 Struttura del Progetto

```
template-accv/
├── assets/
│   ├── fonts/           # Font Google (Bebas Neue, Montserrat) scaricati in automatico
│   ├── logos/           # Loghi squadre (PNG con sfondo trasparente)
│   └── backgrounds/     # Sfondi personalizzati e texture
├── data/
│   └── example_match.json  # Esempio dati partita in formato JSON
├── output/              # Cartella di output delle immagini PNG generate
├── src/
│   └── template_accv/
│       ├── config.py    # Palette colori, dimensioni (Post/Story) e costanti di brand
│       ├── models.py    # Modelli dati Python (Team, Scorer, MatchResult, NextMatch, MVP)
│       ├── utils/
│       │   ├── fonts.py    # Gestore ed estrattore automatizzato font
│       │   └── image_fx.py # Utility grafiche PIL (card glassmorphic, sfumature, badge)
│       └── generators/
│           ├── base.py          # Generator base
│           ├── match_result.py  # Renderizzatore Risultato Finale
│           ├── next_match.py    # Renderizzatore Prossima Partita
│           └── mvp.py           # Renderizzatore Migliore in Campo
├── main.py              # Script di esecuzione principale
└── pyproject.toml       # Dipendenze del progetto (Pillow)
```

---

## 🚀 Guida all'Uso

### 1. Generazione di Tutte le Grafiche

Esegui lo script principale senza argomenti:

```bash
python main.py
```

### 2. Generazione Singola o Selettiva (CLI)

È possibile generare le immagini singolarmente o filtrare per formato usando i parametri `--type` (`-t`) e `--format` (`-f`):

- **Solo Risultato Finale** (sia Post 1:1 che Story 9:16):
  ```bash
  python main.py --type result
  ```

- **Solo Migliore in Campo (MVP)**:
  ```bash
  python main.py --type mvp
  ```

- **Solo Prossima Partita (Matchday)**:
  ```bash
  python main.py --type next
  ```

- **Solo formato Verticale Story (9:16) dell'MVP**:
  ```bash
  python main.py --type mvp --format story
  ```

- **Solo formato Quadrato Post (1:1) del Risultato**:
  ```bash
  python main.py --type result --format post
  ```

#### Opzioni CLI disponibili:
- `--type` / `-t`: `all` (default), `result` (Risultato Finale), `next` (Prossima Partita), `mvp` (Migliore in Campo).
- `--format` / `-f`: `both` (default), `post` (solo 1:1), `story` (solo 9:16).
- `--data` / `-d`: file JSON custom con i dati (default: `data/example_match.json`).
- `--output` / `-o`: cartella per il salvataggio (default: `output/`).

---

## 📝 Personalizzazione Dati (JSON)

Puoi modificare il file `data/example_match.json` per inserire i dati della tua partita:

```json
{
  "tournament": "CAMPIONATO CALCETTO A5",
  "matchday": "GIORNATA 5",
  "date": "Giovedì 23 Luglio 2026",
  "time": "21:30",
  "location": "Centro Sportivo ACCV - Campo A",
  "home_team": {
    "name": "A.C. C.V.",
    "short_name": "ACCV",
    "primary_color": [0, 229, 255]
  },
  "away_team": {
    "name": "REAL MATRID",
    "short_name": "MAT",
    "primary_color": [255, 71, 87]
  },
  "home_score": 7,
  "away_score": 4,
  "home_scorers": [
    { "name": "Rossi M.", "goals": 3 },
    { "name": "Bianchi L.", "goals": 2 }
  ],
  "away_scorers": [
    { "name": "Ferrari F.", "goals": 2 }
  ],
  "mvp": {
    "player_name": "Mario Rossi",
    "jersey_number": "10",
    "position": "Attaccante",
    "goals": 3,
    "assists": 2,
    "rating": "9.5"
  }
}
```

---

## 🛡️ Personalizzazione Brand e Loghi

- **Loghi Squadra**: Inserisci i file PNG dei loghi in `assets/logos/` e specifica il percorso nel modello `Team(logo_path="assets/logos/accv.png")`. Se il logo non è fornito, viene generato uno stemma circolare moderno in automatico.
- **Colori di Brand**: Tutti i colori del tema (accenti cyan, scuri, carte, font) sono personalizzabili in `src/template_accv/config.py`.
