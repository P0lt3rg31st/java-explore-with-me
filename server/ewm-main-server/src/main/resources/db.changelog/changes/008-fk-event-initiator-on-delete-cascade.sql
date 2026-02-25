--changeset petrichor:008-fk-event-initiator-on-delete-cascade
ALTER TABLE events
    DROP CONSTRAINT fk_event_initiator;

ALTER TABLE events
    ADD CONSTRAINT fk_event_initiator
        FOREIGN KEY (initiator_id) REFERENCES users(id)
            ON DELETE CASCADE;