--changeset petrichor:009-create-subscriptions
CREATE TABLE subscriptions
(
    id            BIGSERIAL PRIMARY KEY,
    subscriber_id BIGINT    NOT NULL,
    target_id     BIGINT    NOT NULL,
    created_on    TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_subscriptions_subscriber
        FOREIGN KEY (subscriber_id) REFERENCES users (id) ON DELETE CASCADE,

    CONSTRAINT fk_subscriptions_target
        FOREIGN KEY (target_id) REFERENCES users (id) ON DELETE CASCADE,

    CONSTRAINT uq_subscriptions_pair UNIQUE (subscriber_id, target_id),

    CONSTRAINT chk_subscriptions_not_self CHECK (subscriber_id <> target_id)
);

--changeset petrichor:010-indexes-subscriptions
CREATE INDEX idx_subscriptions_subscriber_id ON subscriptions (subscriber_id);
CREATE INDEX idx_subscriptions_target_id ON subscriptions (target_id);