# Gestione Richieste di Accesso agli Atti - Public Administration Service

## Descrizione

Applicazione web full-stack per la gestione delle richieste di accesso agli atti presentate dai cittadini a un Ente pubblico. 

Il sistema implementa:
- **Backend REST** con Spring Boot 4.1.0, Spring Security, JWT authentication
- **Database** H2 (sviluppo) / PostgreSQL (produzione)
- **Frontend Angular** con routing, reactive forms e gestione errori

## Stack Tecnologico

### Backend
- **Java 17**
- **Spring Boot 4.1.0** (Web, Data JPA, Security)
- **JWT Token** per autenticazione stateless
- **BCrypt** per hash password
- **ModelMapper** per DTO mapping
- **H2 / PostgreSQL** per persistenza dati

### Frontend
- **Angular 16+** (da implementare)
- **TypeScript**
- **Reactive Forms**
- **RxJS** per gestione asincrona
- **Bootstrap 5** / **Material Design**

### Testing
- **JUnit 5**
- **Mockito**
- **Spring Test**

## Prerequisiti

- **Java 17 JDK** [Download](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
- **Maven 3.6+** (incluso nel progetto tramite mvnw)
- **Node.js 16+** e **npm 8+** (per Angular frontend)
- **Git**

## Installazione

### 1. Clonare il repository
```bash
git clone https://github.com/utente/publicadministrationservice.git
cd publicadministrationservice
```

### 2. Configurare il Backend

#### Con Maven Wrapper (consigliato)
```bash
# Build del progetto
./mvnw clean install

# Avviare l'applicazione
./mvnw spring-boot:run
```

#### Con Maven installato localmente
```bash
mvn clean install
mvn spring-boot:run
```

L'applicazione sarà disponibile su `http://localhost:8080`

### 3. Accedere al Database H2 (Development)
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (vuota)
```

## Configurazione

### application.properties
Personalizzare le seguenti proprietà in `src/main/resources/application.properties`:

```properties
# Server
server.port=8080

# Database (H2 Development)
spring.datasource.url=jdbc:h2:mem:testdb

# Database (PostgreSQL Production)
# spring.datasource.url=jdbc:postgresql://localhost:5432/public_admin
# spring.datasource.username=postgres
# spring.datasource.password=password

# JWT
jwt.secret=your-super-secret-key-256-bits-long
jwt.expiration=86400000

# File Upload
allegati.upload.dir=./uploads
```

## Credenziali di Test

Dopo l'avvio dell'applicazione, sono disponibili due utenti di test:

| Username | Password | Ruolo | Permessi |
|----------|----------|-------|----------|
| operatore1 | password123 | OPERATORE | Crea, modifica, carica allegati, porta in istruttoria |
| responsabile1 | password123 | RESPONSABILE | Accoglie, respinge, chiude pratiche |

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"operatore1","password":"password123"}'
```

Risposta:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": 1,
  "username": "operatore1",
  "nomeCompleto": "Mario Rossi",
  "email": "mario.rossi@comune.it",
  "ruolo": "OPERATORE"
}
```

## API REST Endpoints

### Autenticazione
- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Registrazione nuovo utente

### Richieste di Accesso
- `POST /api/richieste` - Creare una nuova richiesta (OPERATORE)
- `GET /api/richieste` - Lista richieste con filtri e paginazione
- `GET /api/richieste/{id}` - Dettaglio richiesta
- `PUT /api/richieste/{id}` - Modificare richiesta (OPERATORE)
- `POST /api/richieste/{id}/cambio-stato` - Cambiare stato (RESPONSABILE)

### Allegati
- `POST /api/richieste/{richiestaId}/allegati` - Caricare allegato (OPERATORE)
- `GET /api/richieste/{richiestaId}/allegati` - Lista allegati
- `GET /api/richieste/{richiestaId}/allegati/{allegatoId}/download` - Download allegato
- `DELETE /api/richieste/{richiestaId}/allegati/{allegatoId}` - Eliminare allegato (OPERATORE)

### Filtraggio Richieste
```bash
# Per stato
GET /api/richieste?stato=IN_ISTRUTTORIA

# Per cognome richiedente
GET /api/richieste?cognomeRichiedente=Rossi

# Per numero protocollo
GET /api/richieste?numeroProtocollo=PROT-001

# Con paginazione
GET /api/richieste?page=0&size=20&sort=dataPresentazione,desc
```

## Workflow degli Stati

```
PRESENTATA
    ↓
IN_ISTRUTTORIA
    ├→ RICHIESTA_INTEGRAZIONE → IN_ISTRUTTORIA o RESPINTA
    ├→ ACCOLTA → CHIUSA
    └→ RESPINTA → CHIUSA
```

### Regole di Transizione

| Stato | Transizioni Consentite | Note |
|-------|------------------------|------|
| PRESENTATA | IN_ISTRUTTORIA | Pratica presa in carico |
| IN_ISTRUTTORIA | RICHIESTA_INTEGRAZIONE, ACCOLTA, RESPINTA | Esito istruttoria |
| RICHIESTA_INTEGRAZIONE | IN_ISTRUTTORIA, RESPINTA | Rientro o rigetto |
| ACCOLTA | CHIUSA | Chiusura positiva |
| RESPINTA | CHIUSA | Chiusura negativa |
| CHIUSA | ❌ Nessuno | Stato finale |

## Modello Dati

### Entità Principale: RichiestaAccessoAtti
- `id` - Identificativo univoco
- `numeroProtocollo` - Numero protocollo (univoco)
- `stato` - Stato attuale (PRESENTATA, IN_ISTRUTTORIA, etc.)
- `nomeRichiedente` - Nome del richiedente
- `cognomeRichiedente` - Cognome del richiedente
- `emailRichiedente` - Email del richiedente
- `telefonoRichiedente` - Telefono del richiedente
- `oggetto` - Oggetto della richiesta
- `descrizione` - Descrizione dettagliata
- `dataPresentazione` - Data di presentazione
- `dataModifica` - Data ultima modifica
- `note` - Note interne
- `creatoDa` - Username di chi ha creato
- `modificatoDa` - Username di chi ha modificato

### Entità: StoricoStato
- `id` - Identificativo univoco
- `richiestaId` - Riferimento a RichiestaAccessoAtti
- `statoPrecedente` - Stato precedente
- `statoNuovo` - Nuovo stato
- `dataCambio` - Data del cambio
- `nota` - Nota del cambio di stato
- `utenteCambio` - Username di chi ha fatto il cambio

### Entità: Allegato
- `id` - Identificativo univoco
- `richiestaId` - Riferimento a RichiestaAccessoAtti
- `nomeFile` - Nome originale del file
- `tipoFile` - MIME type
- `dimensione` - Dimensione in byte
- `pathFile` - Percorso del file nel filesystem
- `dataUpload` - Data caricamento
- `caricatoDa` - Username di chi ha caricato

## Comandi Utili

### Build e Test
```bash
# Build completo
./mvnw clean install

# Eseguire solo i test
./mvnw test

# Build senza eseguire i test
./mvnw clean install -DskipTests

# Eseguire un test specifico
./mvnw test -Dtest=RichiestaAccessoAttiServiceTest
```

### Cleanup
```bash
# Pulire il progetto
./mvnw clean

# Eliminare cache locale
./mvnw clean -U
```

## Frontend Angular (da implementare)

```bash
# Creare progetto Angular
ng new publicadministrationservice-frontend
cd publicadministrationservice-frontend

# Installare dipendenze
npm install

# Avviare dev server (porta 4200)
ng serve

# Build per produzione
ng build --configuration production
```

### Struttura Frontend Consigliata
```
src/
├── app/
│   ├── core/
│   │   ├── services/
│   │   │   ├── auth.service.ts
│   │   │   ├── richiesta.service.ts
│   │   │   └── allegato.service.ts
│   │   └── guards/
│   │       └── auth.guard.ts
│   ├── shared/
│   │   ├── models/
│   │   └── components/
│   ├── features/
│   │   ├── auth/
│   │   │   └── login.component.ts
│   │   ├── richieste/
│   │   │   ├── lista/
│   │   │   ├── dettaglio/
│   │   │   └── form/
│   │   └── allegati/
│   └── app-routing.module.ts
└── main.ts
```

## Docker (Opzionale)

### Docker Compose per PostgreSQL
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: public_admin
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

Avviare con:
```bash
docker-compose up -d
```

## Sicurezza

- **JWT Stateless Authentication**: Nessuna sessione server-side
- **Password Hashing**: BCrypt con salt
- **CORS**: Configurato per localhost:4200 (dev)
- **Method Security**: @PreAuthorize per controllo accesso
- **HTTPS**: Consigliato in produzione
- **CSRF**: Disabilitato per JWT (riabilitare se si usa session)

## Problemi Comuni

### "Address already in use"
La porta 8080 è già in uso. Modificare in `application.properties`:
```properties
server.port=8081
```

### Errore "Cannot parse JWT token"
Verificare che `jwt.secret` sia uguale tra login e richieste autenticate.

### Allegati non salvati
Verificare che la directory `./uploads` esista e sia scrivibile:
```bash
mkdir -p uploads
chmod 755 uploads
```

## Contribuzione

1. Creare un branch per la feature: `git checkout -b feature/nome-feature`
2. Committare i cambiamenti: `git commit -am 'Add feature'`
3. Fare push: `git push origin feature/nome-feature`
4. Aprire una Pull Request

## Licenza

MIT License - Copyright (c) 2024

## Contatti

Per domande o segnalazioni di bug, contattare l'amministratore del progetto.

## Risorse Utili

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Reference](https://docs.spring.io/spring-security/docs/current/reference/)
- [Angular Documentation](https://angular.io/docs)
- [JWT Introduction](https://jwt.io/introduction)
- [RESTful API Best Practices](https://restfulapi.net/)

---

**Versione**: 1.0.0  
**Data**: Giugno 2024  
**Stato**: In Sviluppo

