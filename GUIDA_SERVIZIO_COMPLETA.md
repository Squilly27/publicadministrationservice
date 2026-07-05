# Guida completa al servizio `publicadministrationservice`

## 1. Che cosa fa questo servizio

`publicadministrationservice` è un backend REST sviluppato con Spring Boot per gestire le **richieste di accesso agli atti** di una pubblica amministrazione.

In pratica il servizio permette di:

- autenticare gli utenti con **JWT**
- registrare nuovi utenti con ruoli diversi
- creare e gestire richieste di accesso agli atti
- filtrare e consultare le richieste
- cambiare lo stato di una richiesta seguendo un workflow controllato
- caricare, scaricare e cancellare allegati collegati a una richiesta
- tracciare lo storico dei cambi di stato
- inizializzare il database con dati di test

Il progetto è pensato come **backend stateless**: ogni richiesta autenticata deve portare il token JWT nell'header `Authorization`.

---

## 2. Stack tecnologico

Dal file `pom.xml` e dalla configurazione attuale, il servizio usa:

- **Java 17**
- **Spring Boot 3.5.3**
- **Spring Web** per le API REST
- **Spring Data JPA** per l'accesso ai dati
- **Spring Security** per autenticazione e autorizzazione
- **JWT** per l'accesso stateless
- **Hibernate/JPA** per il mapping oggetti-tabelle
- **H2** in sviluppo/test
- **PostgreSQL** come database supportato per produzione
- **ModelMapper** per convertire entità e DTO
- **Lombok** per ridurre il boilerplate
- **Bean Validation** per validare i payload in input

---

## 3. Architettura del progetto

La struttura principale del backend è questa:

- `controller/` → espone le API REST
- `service/` → contiene la logica di business
- `repository/` → accesso al database
- `entity/` → entità JPA e enum di dominio
- `dto/` → oggetti di scambio per richieste e risposte
- `security/` → JWT, filtro di sicurezza, caricamento utenti
- `config/` → configurazione Spring Security, CORS, encoder password
- `exception/` → gestione centralizzata degli errori

---

## 4. Modello dati

### 4.1 `Utente`

Rappresenta l'utente del sistema.

Campi principali:

- `username`
- `password` hashata con BCrypt
- `nomeCompleto`
- `email`
- `ruolo`
- `attivo`
- `dataCreazione`
- `dataModifica`

### 4.2 `RuoloUtente`

I ruoli disponibili sono:

- `OPERATORE`
- `RESPONSABILE`
- `ADMIN`

### 4.3 `RichiestaAccessoAtti`

È l'entità centrale del sistema.

Contiene:

- `numeroProtocollo`
- `stato`
- dati del richiedente: nome, cognome, email, telefono
- `oggetto`
- `descrizione`
- `note`
- `creatoDa`
- `modificatoDa`
- `dataPresentazione`
- `dataModifica`

Relazioni:

- una richiesta ha molti `Allegato`
- una richiesta ha molti record `StoricoStato`

### 4.4 `Allegato`

Rappresenta un file associato a una richiesta.

Campi principali:

- `nomeFile`
- `tipoFile`
- `dimensione`
- `pathFile`
- `dataUpload`
- `caricatoDa`

### 4.5 `StoricoStato`

Traccia ogni passaggio di stato della richiesta.

Campi principali:

- `statoPrecedente`
- `statoNuovo`
- `nota`
- `utenteCambio`
- `dataCambio`

### 4.6 `StatoRichiesta`

Il workflow previsto è:

- `PRESENTATA`
- `IN_ISTRUTTORIA`
- `RICHIESTA_INTEGRAZIONE`
- `ACCOLTA`
- `RESPINTA`
- `CHIUSA`

---

## 5. Ruoli e permessi

Il sistema distingue tre ruoli.

### `OPERATORE`
Può:

- creare richieste
- modificare richieste
- caricare allegati
- eliminare allegati
- consultare richieste
- vedere gli allegati

### `RESPONSABILE`
Può:

- consultare richieste
- cambiare lo stato delle richieste
- vedere gli allegati

### `ADMIN`
Può:

- fare tutto ciò che possono fare `OPERATORE` e `RESPONSABILE`
- accedere agli endpoint amministrativi presenti sotto `/api/admin/**`

Nel codice, `ADMIN` è stato abilitato anche nei controlli `@PreAuthorize` e nelle regole di sicurezza HTTP.

---

## 6. Autenticazione JWT

### Flusso generale

1. l'utente chiama `POST /api/auth/login`
2. il sistema verifica username e password
3. viene generato un token JWT
4. il client invia il token negli header delle richieste successive:

```http
Authorization: Bearer <token>
```

### Componenti coinvolti

#### `AuthenticationController`
Espone i metodi di login e registrazione.

#### `AuthenticationService`
Gestisce:

- autenticazione dell'utente
- generazione del token
- registrazione di nuovi utenti

#### `JwtTokenProvider`
Crea e valida i token JWT.

#### `JwtAuthenticationFilter`
Intercetta le richieste, estrae il token, lo valida e popola il `SecurityContext`.

#### `CustomUserDetailsService`
Carica l'utente dal database e costruisce le authority Spring Security del tipo `ROLE_OPERATORE`, `ROLE_RESPONSABILE`, `ROLE_ADMIN`.

---

## 7. Endpoint REST

## 7.1 Autenticazione

### `POST /api/auth/login`
Effettua il login.

Payload:

```json
{
  "username": "operatore1",
  "password": "password123"
}
```

Risposta:

```json
{
  "token": "<jwt>",
  "userId": 1,
  "username": "operatore1",
  "nomeCompleto": "Mario Rossi",
  "email": "mario.rossi@comune.it",
  "ruolo": "OPERATORE"
}
```

### `POST /api/auth/register`
Registra un nuovo utente.

Payload previsto da `CreateUtenteDTO`:

- `username`
- `password`
- `nomeCompleto`
- `email`
- `ruolo`

---

## 7.2 Richieste di accesso agli atti

Base path: `/api/richieste`

### `POST /api/richieste`
Crea una nuova richiesta.

Accesso: `ADMIN`, `OPERATORE`

Comportamento:

- genera un numero di protocollo automatico
- imposta lo stato iniziale a `PRESENTATA`
- salva il record
- registra una riga nello storico

### `GET /api/richieste`
Restituisce le richieste in modo paginato.

Parametri opzionali:

- `stato`
- `cognomeRichiedente`
- `numeroProtocollo`
- parametri di paginazione Spring (`page`, `size`, `sort`)

### `GET /api/richieste/{id}`
Restituisce il dettaglio completo di una richiesta.

Include:

- dati principali
- allegati
- storico degli stati

### `PUT /api/richieste/{id}`
Aggiorna una richiesta esistente.

Accesso: `ADMIN`, `OPERATORE`

### `POST /api/richieste/{id}/cambio-stato`
Cambia lo stato di una richiesta.

Accesso: `ADMIN`, `RESPONSABILE`

Comportamento:

- verifica che la transizione sia valida
- aggiorna lo stato
- scrive il cambio nello storico

Payload previsto da `CambioStatoDTO`:

- `statoNuovo`
- `nota`

---

## 7.3 Allegati

Base path: `/api/richieste/{richiestaId}/allegati`

### `POST /api/richieste/{richiestaId}/allegati`
Carica un allegato.

Accesso: `ADMIN`, `OPERATORE`

Comportamento:

- crea la directory della richiesta se non esiste
- genera un nome file univoco con UUID
- salva il file nel filesystem
- registra i metadati nel database

### `GET /api/richieste/{richiestaId}/allegati`
Restituisce tutti gli allegati della richiesta.

### `GET /api/richieste/{richiestaId}/allegati/{allegatoId}/download`
Scarica il file dell'allegato.

### `DELETE /api/richieste/{richiestaId}/allegati/{allegatoId}`
Elimina l'allegato.

Accesso: `ADMIN`, `OPERATORE`

Comportamento:

- cancella il file dal filesystem
- elimina il record dal database

---

## 8. Regole di sicurezza

La sicurezza è configurata in `SecurityConfig`.

### Regole principali

- `/api/auth/**` → accesso libero
- `/api/admin/**` → `ADMIN` o `RESPONSABILE`
- GET `/api/richieste/**` → `ADMIN`, `OPERATORE`, `RESPONSABILE`
- POST `/api/richieste` → `ADMIN`, `OPERATORE`
- PUT `/api/richieste/**` → `ADMIN`, `OPERATORE`
- POST `/api/richieste/*/allegati` → `ADMIN`, `OPERATORE`
- POST `/api/richieste/*/cambio-stato` → `ADMIN`, `RESPONSABILE`
- tutte le altre richieste → serve autenticazione

### Altre impostazioni

- sessioni disabilitate (`STATELESS`)
- CSRF disabilitato per supportare JWT
- CORS abilitato per `http://localhost:4200`

---

## 9. Logica di business delle richieste

Il cuore applicativo è `RichiestaAccessoAttiService`.

### `createRichiesta()`

Fa quanto segue:

- legge l'utente autenticato
- genera il protocollo
- crea la richiesta in stato `PRESENTATA`
- salva lo storico iniziale

### `updateRichiesta()`

Aggiorna i dati della richiesta:

- nome richiedente
- cognome richiedente
- email
- telefono
- oggetto
- descrizione
- utente che ha effettuato la modifica

### `getRichiestaById()`

Recupera il dettaglio di una richiesta, convertendo l'entità in DTO.

### `getAllRichieste()`

Restituisce tutte le richieste in formato paginato.

### `filterRichieste()`

Applica i filtri opzionali presenti nella query string.

### `cambiaStato()`

È il metodo più importante del workflow.

Fa controllo su:

- esistenza della richiesta
- transizione di stato consentita
- aggiornamento dello stato
- salvataggio dello storico

Le transizioni consentite sono definite in una mappa interna del service.

---

## 10. Gestione allegati

Il servizio `AllegatoService` si occupa di:

- salvare i file nel filesystem
- recuperare gli allegati di una richiesta
- recuperare un allegato singolo
- cancellare il file e il relativo record DB

La directory di upload è configurata in:

```properties
allegati.upload.dir=./uploads
```

Se il file system non contiene la cartella, il servizio la crea automaticamente.

---

## 11. Dati iniziali

### `src/main/resources/init-data.sql`

Lo script inserisce dati di test per H2:

- `operatore1`
- `responsabile1`
- `admin1`

e due richieste di esempio con storico iniziale.

### Credenziali di test

- `operatore1` / `password123`
- `responsabile1` / `password123`
- `admin1` / `password123`

### `src/main/resources/init-db.sql`

Contiene lo schema PostgreSQL:

- tabella `utente`
- tabella `richiesta_accesso_atti`
- tabella `storico_stato`
- tabella `allegato`
- indici per performance

---

## 12. Configurazione applicativa

File principale: `src/main/resources/application.properties`

### Parametri principali

- porta: `8081`
- database di default: H2 in memoria
- script di inizializzazione dati: `init-data.sql`
- JWT secret configurato
- scadenza token: `86400000` ms, cioè 24 ore
- upload directory: `./uploads`
- logging del package applicativo in `DEBUG`

---

## 13. Error handling

Gli errori sono gestiti centralmente tramite un handler globale.

Il comportamento atteso è:

- `401 Unauthorized` se il token è mancante o non valido
- `403 Forbidden` se l'utente non ha i permessi
- `400 Bad Request` per input non validi
- `404 Not Found` per risorse mancanti
- `500 Internal Server Error` per errori imprevisti

---

## 14. Flusso operativo tipico

### Scenario 1: un operatore crea una richiesta

1. fa login
2. riceve il token JWT
3. chiama `POST /api/richieste`
4. la richiesta viene salvata in stato `PRESENTATA`
5. viene creato il primo record di storico

### Scenario 2: carica allegati

1. recupera l'ID della richiesta
2. chiama `POST /api/richieste/{id}/allegati`
3. il file viene scritto su disco
4. i metadati vengono salvati nel database

### Scenario 3: il responsabile lavora la pratica

1. consulta le richieste
2. apre il dettaglio
3. cambia lo stato con `POST /api/richieste/{id}/cambio-stato`
4. il sistema verifica la transizione
5. il cambio viene registrato nello storico

### Scenario 4: l'admin gestisce tutto

L'utente `ADMIN` può usare tutte le operazioni principali del sistema senza limitazioni funzionali sui moduli esposti.

---

## 15. Come avviare il progetto

Da PowerShell:

```powershell
Set-Location "C:\Users\franc\Desktop\ifm\publicadministrationservice"
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

L'applicazione ascolta su:

- `http://localhost:8081`
- console H2: `http://localhost:8081/h2-console`

---

## 16. Esempi di chiamate utili

### Login

```bash
curl -X POST http://localhost:8081/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin1\",\"password\":\"password123\"}"
```

### Creazione richiesta

```bash
curl -X POST http://localhost:8081/api/richieste ^
  -H "Authorization: Bearer <token>" ^
  -H "Content-Type: application/json" ^
  -d "{\"nomeRichiedente\":\"Luca\",\"cognomeRichiedente\":\"Bianchi\",\"emailRichiedente\":\"luca.bianchi@example.com\",\"telefonoRichiedente\":\"3331234567\",\"oggetto\":\"Accesso agli atti\",\"descrizione\":\"Richiesta documentazione\"}"
```

### Cambio stato

```bash
curl -X POST http://localhost:8081/api/richieste/1/cambio-stato ^
  -H "Authorization: Bearer <token>" ^
  -H "Content-Type: application/json" ^
  -d "{\"statoNuovo\":\"IN_ISTRUTTORIA\",\"nota\":\"Pratica presa in carico\"}"
```

---

## 17. Cosa non c'è nel repository

Nel repository attuale non risulta presente un frontend completo: il progetto è sostanzialmente un **backend Spring Boot** con API REST.

Quindi questa guida descrive il comportamento reale del servizio lato server.

---

## 18. Riassunto finale

In sintesi, questo servizio:

- gestisce utenti, autenticazione e ruoli
- protegge le API con JWT e Spring Security
- consente la creazione e la lavorazione delle richieste di accesso agli atti
- gestisce allegati su filesystem e database
- traccia lo storico dei cambi di stato
- offre dati iniziali e configurazioni per sviluppo/test

Se vuoi, posso anche trasformare questa guida in una versione più **operativa per il team** con:

- diagramma del flusso
- elenco endpoint in tabella
- esempi request/response completi
- sezione dedicata a deploy e produzione

-secret = P6p58A1D2h4FV/SlrBl9oHGUsN9MJolqsEzXl7/T71o61J2FNe9R6b7DxeC59tRI/lOUTTIGL5LEIgsOTJvSiw==
