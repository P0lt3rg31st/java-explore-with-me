--changeset petrichor:001-create-hits
CREATE TABLE IF NOT EXISTS hits
(
    id        BIGSERIAL PRIMARY KEY,
    app       VARCHAR(255) NOT NULL,
    uri       VARCHAR(255) NOT NULL,
    ip        VARCHAR(64)  NOT NULL,
    timestamp TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_hits_timestamp
    ON hits (timestamp);

CREATE INDEX IF NOT EXISTS idx_hits_uri
    ON hits (uri);

CREATE INDEX IF NOT EXISTS idx_hits_app_uri_timestamp
    ON hits (app, uri, timestamp);
