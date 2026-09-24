# ACCV - Calcetto Social Graphics Automation ⚽

Automazione per la creazione automatizzata di template grafici social per i risultati delle partite e gli eventi della squadra di calcetto **A.C.C.V.**. Supporta tutti i formati social media (**9:16**, **4:3**, **16:9**, **1:1**, **4:5**), la gestione intelligente degli sfondi basata su categorie emotive, la ricerca automatica dei loghi delle squadre del campionato e l'input diretto da riga di comando.

---

## 🎨 Tipologie di Grafiche Supportate

1. **Risultato Finale (`MatchResult`)**:
   - Punteggio partita in evidenza con badge al neon.
   - Stemmi delle squadre del campionato caricati da `assets/logos/` (o generati in automatico).
   - Lista marcatori divisa per squadra (con conteggio gol per ciascun giocatore).
   - Menzione del *Migliore in Campo (MVP)*.
   - Data, ora, campo e competizione.

2. **Prossima Partita (`NextMatch`)**:
   - Grafica promozionale con abbinamento squadre (VS).
   - Dettagli evento (data, ora di calcio d'inizio, impianto sportivo).

3. **Migliore in Campo (`MVP`)**:
   - Scheda giocatore in stile carte FUT / eSports con accenti dorati.
   - Numero di maglia (#10, #7, ecc.), ruolo e statistiche (gol, assist, parate, voto pagella).

4. **Figurine Calciatori Panini (`FigurinaCard`)**:
   - Riproduzione autentica delle figurine Panini Serie B Perini con template ufficiale.
   - Posizionamento, riscalatura e ritaglio automatico dei calciatori da `assets/players/headshot/transparent/`.
   - Targhetta Panini con nome, ruolo, numero di maglia e stemma societario ACCV.
   - Formati dedicati sia per stampa/collezione (Card 1024x1536) sia per i social (Storie Instagram 9:16 con effetto 3D e Post 4:5).
   - Tool dedicato semplicissimo da usare anche per non tecnici: `python3 generate_figurine.py`.

---

## 📐 Formati Social Media Supportati

Il generatore supporta 5 diversi formati di output adattivi:

| Formato | Risoluzione | Utilizzo consigliato |
| :--- | :--- | :--- |
| **9:16** | 1080 x 1920 | Instagram Story, Reels, TikTok, WhatsApp Status |
| **4:3** | 1440 x 1080 | Post Orizzontale Standard, Facebook Feed |
| **16:9** | 1920 x 1080 | Widescreen, Twitter / X, YouTube Thumbnail, Banner |
| **1:1** | 1080 x 1080 | Instagram Post Quadrato |
| **4:5** | 1080 x 1350 | Instagram Post Verticale High-Portrait |

---

## 🖼️ Gestione Sfondi, Categorie Emotive e Filtri

### Scelta dello Sfondo:
1. **Input Immagine Specifica**: Con l'argomento `--bg-image` / `--bg` viene utilizzata l'immagine indicata.
2. **Selezione per Emozione**: Con l'argomento `--emotion` / `-e` (es. `felicità`, `tristezza`, `polemica`, `normale`, `foto squadra`), lo script seleziona casualmente una foto presente nella relativa sottocartella in `assets/backgrounds/` (es. `assets/backgrounds/tema felicità/`).
3. **Default Behaviour**: Se non viene specificata alcuna categoria (o se la cartella non esiste), lo script seleziona l'immagine di sfondo presente nella cartella `/backgrounds` (`assets/backgrounds/std.JPG`).

## Filtri sul Contrasto:
- `--contrast <valore>`: Imposta il fattore di contrasto per lo sfondo (es. `0.5` per sfondi più morbidi, `1.0` normale).
- `--no-contrast`: Riduce automaticamente il contrasto dell'immagine di sfondo per un effetto flat.

---

---

## 🌐 Studio Web Locale (Consigliato per Social Media Manager) 🚀

Per generare, visualizzare e scaricare le grafiche in tempo reale senza usare comandi complessi né modificare file JSON, avvia lo **Studio Grafico Web**:

```bash
python3 main.py serve
```
- **URL Desktop**: Apri nel browser `http://localhost:8000`
- **Accesso da Smartphone (Spogliatoio / Terzo Tempo)**: Apri `http://<tuo-ip-locale>:8000` (es. `http://192.168.1.15:8000`)

### ⚡ Funzionalità dello Studio Web:
- **Live Preview Sub-Second**: Rendering in memoria ad ogni modifica senza scrivere su disco.
- **Selettori Intelligenti**: Autocomplete delle squadre avversarie da `assets/logos/` e dei calciatori da `data/players.json`.
- **Tasti Rapidi Marcatori**: Clicca sui chip dei giocatori per aggiungere gol con stepper `+` / `-`.
- **Instagram Safe Zone Guide**: Overlay interattivo per verificare che testi e loghi non siano coperti dall'interfaccia delle storie.
- **Export Multiplo & Condivisione**: Download immediato PNG, pacchetto ZIP con tutti i formati, copia negli appunti e condivisione social diretta su smartphone via Web Share API.
- **Studio Figurine Panini**: Visualizzazione della rosa, anteprima delle figurine e download batch.
- **Gestore Rosa & Campionato**: Operazioni CRUD per aggiungere/modificare/eliminare giocatori e squadre del campionato.

### 🌍 Come Condividere la Demo Pubblicamente Online:

#### Opzione A: Tunnel Istantaneo (Consigliato per test rapido, 10 secondi)
Con il server attivo su `localhost:8000`, puoi generare all'istante un link pubblico HTTPS senza bisogno di server dedicati:
```bash
# Con Cloudflare Tunnel (gratuito, nessun account necessario):
cloudflared tunnel --url http://localhost:8000

# Oppure con npx (Node.js):
npx localtunnel --port 8000
```
Condividi il link generato (es. `https://xyz.trycloudflare.com`) su WhatsApp per far testare la web app a tutti i componenti della squadra direttamente da mobile!

#### Opzione B: Deploy Permanente Gratuito (Render / Fly.io / HuggingFace)
Il repository include già i file di configurazione `render.yaml`, `Dockerfile` e `Procfile`.
Basta collegare la repo GitHub su **Render.com** (Free Web Service) per avere l'app online 24/7 con URL permanente.

---

## 🚀 Guida all'Uso ed Esempi di Esecuzione da CLI

Puoi utilizzare la CLI unificata sia tramite comandi dedicati (`serve`, `generate`, `figurina`) sia con la sintassi diretta classica:

### 1. Avvio Studio Web
```bash
python3 main.py serve --port 8000 --open
```

### 2. Esecuzione Base (Tutte le Grafiche nei Formati Default)
Genera le grafiche per Risultato, Prossima Partita ed MVP basandosi sui dati in JSON:
```bash
python3 main.py generate
# oppure semplicemente:
python3 main.py
```

---

### 2. Risultato Finale con Input Diretto da Riga di Comando
Specifica direttamente le squadre ed il punteggio senza modificare alcun file JSON:
```bash
# Esempio risultato vittoria ACCV vs Real Matrid
uv run python main.py --type result --home-team "A.C. C.V." --away-team "Real Matrid" --score 5-2

# Sintassi alternativa con gol separati
uv run python main.py --type result --home-team "A.C. C.V." --away-team "FC Barcelona" --home-score 4 --away-score 3
```

### 3. Layout Ufficiale Photo-Overlay
Il generatore adotta come standard ufficiale il layout **Minimal Photo-Overlay** (foto d'azione ad alta risoluzione, desaturazione morbida, loghi societari affiancati al punteggio e scritta "MATCH RESULT"):

```bash
# Esempio risultato finale in formato 4:5
python3 main.py generate --type result --home-team "TOTUTTI" --away-team "ACCV" --score 3-4 --format 4:5
```

---

### 4. Selezione dello Sfondo basata su Categoria Emotiva
Sceglie una foto random dalla sottocartella di `assets/backgrounds/`:
```bash
# Vittoria schiacciante (scelta foto random da 'tema felicità')
uv run python main.py --score 6-1 --emotion felicita

# Sconfitta (scelta foto random da 'tema tristezza')
uv run python main.py --score 1-4 --emotion tristezza

# Partita accesa (scelta foto random da 'tema polemica')
uv run python main.py --score 3-3 --emotion polemica

# Foto di gruppo / squadra (scelta foto random da 'foto squadra')
uv run python main.py --score 4-2 --emotion "foto squadra"
```

---

### 4. Generazione per Formati Social Media Specifici (o Tutti insieme)
```bash
# Genera solo il formato Story / Reels / TikTok (9:16)
uv run python main.py --type result --format 9:16

# Genera solo il formato Post Orizzontale (4:3)
uv run python main.py --type result --format 4:3

# Genera solo il formato Post Quadrato Instagram (1:1)
uv run python main.py --type result --format 1:1

# Genera TUTTI e 5 i formati social (9:16, 4:3, 16:9, 1:1, 4:5) contemporaneamente
uv run python main.py --type result --format all
```

---

### 5. Immagine di Sfondo Personalizzata e Filtro Contrassto
```bash
# Usa un file di sfondo specifico
uv run python main.py --bg assets/backgrounds/std.JPG

# Applica il filtro per ridurre il contrasto dello sfondo
uv run python main.py --score 3-2 --no-contrast

# Personalizza numericamente il contrasto (es. 0.6 per effetto soft)
uv run python main.py --contrast 0.6
```

---

### 6. Grafica Prossima Partita (`NextMatch`) e Migliore in Campo (`MVP`)
```bash
# Prossima partita imminente in formato 9:16
uv run python main.py --type next --home-team "A.C. C.V." --away-team "Inter Calcetto" --format 9:16

# Card MVP Migliore in Campo in formato Post Verticale 4:5
uv run python main.py --type mvp --format 4:5
```

---

### 7. Generazione Figurine Calciatori Panini (`FigurinaCard`) 🌟

Puoi creare le figurine dei giocatori in due modi semplicissimi:

#### Metodo A: Script Dedicato Semplificato (Consigliato anche per chi non è esperto!)
Lo script `generate_figurine.py` è pensato per essere intuitivo e immediato per chiunque gestisca la comunicazione della squadra:
```bash
# 1. Genera in 1 clic tutte le figurine della squadra (sia Card classica che Storie Instagram!):
python3 generate_figurine.py

# 2. Avvia la procedura guidata interattiva (menu a scelta numerata semplice):
python3 generate_figurine.py -i

# 3. Genera la figurina di un singolo giocatore:
python3 generate_figurine.py --player bouba

# 4. Genera solo il formato per le Storie Instagram (9:16 con effetto 3D):
python3 generate_figurine.py --format story
```

#### Metodo B: Tramite lo script principale `main.py`
```bash
# Genera tutte le figurine (formato card classica + storie Instagram)
python3 main.py --type figurina --format all

# Genera un singolo giocatore in formato storia
python3 main.py --type figurina --player elia --format story

# Genera la figurina pulita senza la targhetta inferiore con nome/ruolo
python3 main.py --type figurina --player g-giannessi --no-banner
```

> [!TIP]
> **Personalizzazione Giocatori**: Puoi modificare nomi, ruoli e numeri di maglia semplicemente aprendo il file [data/players.json](file:///home/gabriele/Documenti/projects/template-accv/data/players.json). Se aggiungi una nuova foto in `assets/players/headshot/transparent/`, verrà rilevata automaticamente!

---

### 📋 Elenco Completo Opzioni CLI:

| Opzione | Scorciatoia | Valori consentiti | Descrizione |
| :--- | :--- | :--- | :--- |
| `--type` | `-t` | `all`, `result`, `next`, `mvp`, `figurina` | Tipo di grafica da generare (default: `all`) |
| `--format` | `-f` | `both`, `all`, `card`, `story`, `post` | Formato di output social |
| `--player` | `-p` | *Stringa* | Nome o ID giocatore per figurina (es. `bouba`, `elia`) |
| `--no-banner` | | *Flag* | Omette la targhetta nome/ruolo/logo sulla figurina |
| `--data` | `-d` | *Path file* | File JSON sorgente dati (default: `data/example_match.json`) |
| `--output` | `-o` | *Path cartella* | Cartella di output delle grafiche (default: `output/`) |
| `--home-team` | `-ht` | *Stringa* | Nome squadra di casa (es. `"A.C. C.V."`) |
| `--away-team` | `-at` | *Stringa* | Nome squadra ospite (es. `"Real Matrid"`) |
| `--score` | `-s` | *Stringa* | Punteggio risultato (es. `"5-2"` o `"5:2"`) |
| `--home-score` | | *Intero* | Gol segnati dalla squadra di casa |
| `--away-score` | | *Intero* | Gol segnati dalla squadra ospite |
| `--bg-image` | `--bg` | *Path file* | Percorso immagine di sfondo specifica |
| `--emotion` | `-e` | `felicità`, `tristezza`, `polemica`, `normale`, `foto squadra` | Categoria emozionale sfondo |
| `--contrast` | | *Float* | Fattore di contrasto per lo sfondo (es. `0.8`) |
| `--no-contrast` | | *Flag* | Abilita il filtro per ridurre il contrasto |
| `--data` | `-d` | *Path file* | File JSON sorgente dati (default: `data/example_match.json`) |
| `--output` | `-o` | *Path cartella* | Cartella di output delle grafiche (default: `output/`) |

---

## 📁 Struttura del Progetto

```
template-accv/
├── assets/
│   ├── fonts/                     # Font Google (Bebas Neue, Montserrat) scaricati in automatico
│   ├── logos/                     # Loghi delle squadre del campionato (PNG con sfondo trasparente)
│   └── backgrounds/               # Sfondo di default (std.JPG) e sottocartelle per categorie emotive (tema felicità, foto squadra, ecc.)
├── data/
│   └── example_match.json         # Esempio dati partita in formato JSON
├── output/                        # Cartella di output delle immagini PNG generate
├── src/
│   └── template_accv/
│       ├── config.py              # Palette colori, formati e dimensioni (9:16, 4:3, 16:9, 1:1, 4:5)
│       ├── models.py              # Modelli dati Python (Team, Scorer, MatchResult, NextMatch, MVP)
│       ├── utils/
│       │   ├── fonts.py           # Gestore ed estrattore automatizzato font
│       │   ├── image_fx.py        # Utility grafiche PIL (cards glassmorphism, stemmi)
│       │   ├── backgrounds.py     # Gestore sfondi emotivi, filtri contrasto e overlay
│       │   └── logo_generator.py  # Generatore automatico loghi campionato
│       └── generators/
│           ├── base.py            # Generator base
│           ├── match_result.py    # Renderizzatore Risultato Finale
│           ├── next_match.py      # Renderizzatore Prossima Partita
│           └── mvp.py             # Renderizzatore Migliore in Campo
├── main.py                        # Script di esecuzione principale
└── pyproject.toml                 # Dipendenze del progetto (Pillow)
```

---

## 🛡️ Loghi Squadre del Campionato

Inserisci i file PNG dei loghi nella cartella `assets/logos/` (es. `accv.png`, `real_matrid.png`, `fc_barcelona.png`). Quando si specifica il nome della squadra via CLI o JSON, lo script cercherà automaticamente il file stemma corrispondente in `assets/logos/`. Se non presente, verrà creato uno stemma circolare moderno ad hoc.
