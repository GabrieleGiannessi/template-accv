# ACCV Designer Lab — Frontend

Client desktop in Angular standalone with Angular Material. The screen keeps the existing arrangement: section navigation at the top, editor on the left, persistent preview and export actions on the right.

## Avvio in sviluppo

Prerequisiti: Node.js `^20.19`, `^22.12` oppure `>=24`, con npm incluso nell'installazione di Node.js.

1. Avvia il backend Spring Boot sulla porta `8080` e configura database e cartella `assets` come descritto in `../backend/`.
2. Da questa cartella esegui `npm install` una prima volta.
3. Esegui `npm start` e apri `http://localhost:4200`.

Il proxy locale inoltra `/api` e `/assets` al backend su `http://localhost:8080`; cambia la destinazione in `proxy.conf.json` se il backend ascolta su un'altra porta.

## Build

`npm run build` genera la build di produzione in `dist/accv-frontend`.

La prima versione collega i quattro generatori grafici e consente la gestione CRUD di giocatori, squadre, stagioni e competizioni, incluse le associazioni tra stagioni e squadre. Il mobile dedicato sarà realizzato dopo le rifiniture desktop, con priorità alla generazione del Risultato.
