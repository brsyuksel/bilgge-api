CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    username VARCHAR NOT NULL,
    public_key VARCHAR NOT NULL,
    key VARCHAR NOT NULL,
    salt VARCHAR NOT NULL,
    login_token VARCHAR,
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
    updated_at TIMESTAMP NOT NULL DEFAULT current_timestamp
);

CREATE UNIQUE INDEX users_username_idx
    ON users (username);

CREATE TABLE collections (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id UUID NOT NULL,
    name VARCHAR NOT NULL,
    iv VARCHAR NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
    updated_at TIMESTAMP NOT NULL DEFAULT current_timestamp
);

  ALTER TABLE collections
    ADD CONSTRAINT fk_user_id
FOREIGN KEY (user_id) REFERENCES users(id)
     ON DELETE CASCADE;

CREATE TABLE secrets (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id UUID NOT NULL,
    collection_id UUID NOT NULL,
    type VARCHAR NOT NULL,
    title VARCHAR NOT NULL,
    content varchar NOT NULL,
    hashes jsonb,
    iv VARCHAR NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
    updated_at TIMESTAMP NOT NULL DEFAULT current_timestamp
);

  ALTER TABLE secrets
    ADD CONSTRAINT fk_user_id
FOREIGN KEY (user_id) REFERENCES users(id)
     ON DELETE CASCADE;

  ALTER TABLE secrets
    ADD CONSTRAINT fk_collection_id
FOREIGN KEY (collection_id) REFERENCES collections(id)
     ON DELETE CASCADE;

 CREATE INDEX user_id_with_data_idx
     ON secrets (user_id)
INCLUDE (collection_id, hashes);
