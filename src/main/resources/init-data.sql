-- Script di inizializzazione database per Public Administration Service

-- Pulizia per rendere lo script idempotente nei test
DELETE FROM STORICO_STATO;
DELETE FROM ALLEGATO;
DELETE FROM RICHIESTA_ACCESSO_ATTI;
DELETE FROM UTENTE;
ALTER TABLE STORICO_STATO ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE ALLEGATO ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE RICHIESTA_ACCESSO_ATTI ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE UTENTE ALTER COLUMN ID RESTART WITH 1;

-- Inserimento utenti di test
INSERT INTO UTENTE (USERNAME, PASSWORD, NOME_COMPLETO, EMAIL, RUOLO, ATTIVO, DATA_CREAZIONE) VALUES
('operatore1', '$2a$10$MDHQTcbIkr8DGctuceqJ0uF625VHDixacrs3QoZEgHC7g/SmNbnKC', 'Mario Rossi', 'mario.rossi@comune.it', 'OPERATORE', TRUE, CURRENT_TIMESTAMP),
('responsabile1', '$2a$10$MDHQTcbIkr8DGctuceqJ0uF625VHDixacrs3QoZEgHC7g/SmNbnKC', 'Anna Bianchi', 'anna.bianchi@comune.it', 'RESPONSABILE', TRUE, CURRENT_TIMESTAMP);

-- Nota: Le password sono state codificate con BCrypt
-- Username: operatore1, Password: password123
-- Username: responsabile1, Password: password123

-- Inserimento richieste di esempio
INSERT INTO RICHIESTA_ACCESSO_ATTI (NUMERO_PROTOCOLLO, STATO, NOME_RICHIEDENTE, COGNOME_RICHIEDENTE, EMAIL_RICHIEDENTE, TELEFONO_RICHIEDENTE, OGGETTO, DESCRIZIONE, DATA_PRESENTAZIONE, DATA_MODIFICA, CREATO_DA) VALUES
('PROT-001-2024', 'PRESENTATA', 'Giovanni', 'Verdi', 'giovanni.verdi@example.com', '3335551234', 'Accesso ai dati di gestione pratiche', 'Richiesta di accesso ai documenti relativi alla pratica di permesso di costruire', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'operatore1'),
('PROT-002-2024', 'IN_ISTRUTTORIA', 'Francesca', 'Neri', 'francesca.neri@example.com', '3335555678', 'Copia licenza attività commerciale', 'Richiesta copia della licenza di attività commerciale', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'operatore1');

-- Inserimento storico stati
INSERT INTO STORICO_STATO (RICHIESTA_ID, STATO_PRECEDENTE, STATO_NUOVO, DATA_CAMBIO, NOTA, UTENTE_CAMBIO) VALUES
(1, NULL, 'PRESENTATA', CURRENT_TIMESTAMP, 'Pratica presentata', 'operatore1'),
(2, NULL, 'PRESENTATA', CURRENT_TIMESTAMP, 'Pratica presentata', 'operatore1'),
(2, 'PRESENTATA', 'IN_ISTRUTTORIA', CURRENT_TIMESTAMP, 'Pratica presa in carico per istruttoria', 'responsabile1');

