-- Script di inizializzazione per PostgreSQL

CREATE TABLE IF NOT EXISTS utente (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nome_completo VARCHAR(150) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    ruolo VARCHAR(50) NOT NULL,
    attivo BOOLEAN DEFAULT TRUE,
    data_creazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_modifica TIMESTAMP
);

CREATE TABLE IF NOT EXISTS richiesta_accesso_atti (
    id SERIAL PRIMARY KEY,
    numero_protocollo VARCHAR(100) UNIQUE NOT NULL,
    stato VARCHAR(50) NOT NULL,
    nome_richiedente VARCHAR(100) NOT NULL,
    cognome_richiedente VARCHAR(100) NOT NULL,
    email_richiedente VARCHAR(100) NOT NULL,
    telefono_richiedente VARCHAR(20),
    oggetto VARCHAR(255) NOT NULL,
    descrizione TEXT,
    data_presentazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_modifica TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    note TEXT,
    creato_da VARCHAR(50) NOT NULL,
    modificato_da VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS storico_stato (
    id SERIAL PRIMARY KEY,
    richiesta_id INTEGER NOT NULL REFERENCES richiesta_accesso_atti(id) ON DELETE CASCADE,
    stato_precedente VARCHAR(50),
    stato_nuovo VARCHAR(50) NOT NULL,
    data_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nota TEXT,
    utente_cambio VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS allegato (
    id SERIAL PRIMARY KEY,
    richiesta_id INTEGER NOT NULL REFERENCES richiesta_accesso_atti(id) ON DELETE CASCADE,
    nome_file VARCHAR(255) NOT NULL,
    tipo_file VARCHAR(100) NOT NULL,
    dimensione BIGINT NOT NULL,
    path_file VARCHAR(500) NOT NULL,
    data_upload TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    caricato_da VARCHAR(50) NOT NULL
);

-- Indici per migliori performance
CREATE INDEX idx_richiesta_stato ON richiesta_accesso_atti(stato);
CREATE INDEX idx_richiesta_cognome ON richiesta_accesso_atti(cognome_richiedente);
CREATE INDEX idx_richiesta_data ON richiesta_accesso_atti(data_presentazione);
CREATE INDEX idx_storico_richiesta ON storico_stato(richiesta_id);
CREATE INDEX idx_allegato_richiesta ON allegato(richiesta_id);

-- Inserimento utenti di test
INSERT INTO utente (username, password, nome_completo, email, ruolo, attivo) VALUES
('operatore1', '$2a$10$MDHQTcbIkr8DGctuceqJ0uF625VHDixacrs3QoZEgHC7g/SmNbnKC', 'Mario Rossi', 'mario.rossi@comune.it', 'OPERATORE', TRUE),
('responsabile1', '$2a$10$MDHQTcbIkr8DGctuceqJ0uF625VHDixacrs3QoZEgHC7g/SmNbnKC', 'Anna Bianchi', 'anna.bianchi@comune.it', 'RESPONSABILE', TRUE)
ON CONFLICT (username) DO NOTHING;

