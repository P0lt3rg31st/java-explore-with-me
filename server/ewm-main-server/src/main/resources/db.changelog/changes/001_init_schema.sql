--liquibase formatted sql

--changeset petrichor:001-create-users
CREATE TABLE users
(
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(250) NOT NULL,
    email VARCHAR(254) NOT NULL,
    CONSTRAINT uq_email UNIQUE (email)
);

--changeset petrichor:002-create-categories
CREATE TABLE categories
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT uq_category_name UNIQUE (name)
);

--changeset petrichor:003-create-events
CREATE TABLE events
(
    id                 BIGSERIAL PRIMARY KEY,

    annotation         VARCHAR(2000)    NOT NULL,
    description        VARCHAR(7000)    NOT NULL,
    title              VARCHAR(120)     NOT NULL,

    event_date         TIMESTAMP        NOT NULL,
    created_on         TIMESTAMP        NOT NULL DEFAULT now(),
    published_on       TIMESTAMP        NULL,

    paid               BOOLEAN          NOT NULL DEFAULT FALSE,
    participant_limit  INTEGER          NOT NULL DEFAULT 0,
    request_moderation BOOLEAN          NOT NULL DEFAULT TRUE,

    state              VARCHAR(16)      NOT NULL DEFAULT 'PENDING',

    initiator_id       BIGINT           NOT NULL,
    category_id        BIGINT           NOT NULL,

    lat                DOUBLE PRECISION NOT NULL,
    lon                DOUBLE PRECISION NOT NULL,

    CONSTRAINT fk_event_initiator FOREIGN KEY (initiator_id) REFERENCES users (id),
    CONSTRAINT fk_event_category FOREIGN KEY (category_id) REFERENCES categories (id),

    CONSTRAINT chk_event_state CHECK (state IN ('PENDING', 'PUBLISHED', 'CANCELED')),
    CONSTRAINT chk_participant_limit CHECK (participant_limit >= 0),
    CONSTRAINT chk_lat CHECK (lat >= -90 AND lat <= 90),
    CONSTRAINT chk_lon CHECK (lon >= -180 AND lon <= 180)
);

--changeset petrichor:004-create-participation-requests
CREATE TABLE participation_requests
(
    id           BIGSERIAL PRIMARY KEY,

    created      TIMESTAMP   NOT NULL DEFAULT now(),

    event_id     BIGINT      NOT NULL,
    requester_id BIGINT      NOT NULL,

    status       VARCHAR(16) NOT NULL DEFAULT 'PENDING',

    CONSTRAINT fk_request_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_request_requester FOREIGN KEY (requester_id) REFERENCES users (id) ON DELETE CASCADE,

    CONSTRAINT uq_request UNIQUE (event_id, requester_id)
);

--changeset petrichor:005-create-compilations
CREATE TABLE compilations
(
    id     BIGSERIAL PRIMARY KEY,
    title  VARCHAR(50) NOT NULL,
    pinned BOOLEAN     NOT NULL DEFAULT FALSE
);

--changeset petrichor:006-create-compilation-events
CREATE TABLE compilation_events
(
    compilation_id BIGINT NOT NULL,
    event_id       BIGINT NOT NULL,

    PRIMARY KEY (compilation_id, event_id),

    CONSTRAINT fk_compilation_events_compilation FOREIGN KEY (compilation_id)
        REFERENCES compilations (id) ON DELETE CASCADE,

    CONSTRAINT fk_compilation_events_event FOREIGN KEY (event_id)
        REFERENCES events (id) ON DELETE CASCADE
);

--changeset petrichor:007-indexes
CREATE INDEX idx_events_event_date ON events (event_date);
CREATE INDEX idx_events_state ON events (state);
CREATE INDEX idx_events_category_id ON events (category_id);
CREATE INDEX idx_events_initiator_id ON events (initiator_id);

CREATE INDEX idx_requests_event_id ON participation_requests (event_id);
CREATE INDEX idx_requests_requester_id ON participation_requests (requester_id);