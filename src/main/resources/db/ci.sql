INSERT INTO users (id, username, public_key, key, salt, login_token)
VALUES (uuid_generate_V4(), 'ybaroj', 'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCFPjgJZAYwiLRO7nMF9y9p2SSrrAwBHqoPjAG+Fos5glD7UCm3/hsv4kSndV5SrhRfe6J584Ng3juick0YJPE9VnmxU4vNU45r9qrvgPyovKXMr5iPej10+mucVqTfQpcnGaXHb8YLbM/dfRLXmy8wyEE/U9OYIjR2VlAQK9gsjwIDAQAB', 'ybaroj-aes-key', 'ybaroj-salt', NULL),
       ('75ab36d3-3552-407b-b64b-0c56424fc479'::uuid, 'pact-verifier-user', 'pact-verifier-pub-key', 'pact-verifier-aes-key', 'pact-verifier-salt', 'f0523ff3615677c407aeb10d5fc49d0954b62fb1b4855bfc66007443b3c5949f');

INSERT INTO collections (id, user_id, name, iv)
VALUES ('5f6a97a3-52eb-44b2-983f-de9fc5bea7b8'::uuid, '75ab36d3-3552-407b-b64b-0c56424fc479'::uuid, 'encrypted-name-1', 'encrypted-iv-1'),
       ('25bf8f6c-c228-4e6a-9a06-ec26f727a82f'::uuid, '75ab36d3-3552-407b-b64b-0c56424fc479'::uuid, 'will-be-deleted-by-pact', 'will-be-deleted');

INSERT INTO secrets (id, user_id, collection_id, type, title, content, hashes, iv)
VALUES ('528bd2c2-9fc2-471b-866d-e19152a041e3'::uuid, '75ab36d3-3552-407b-b64b-0c56424fc479'::uuid, '5f6a97a3-52eb-44b2-983f-de9fc5bea7b8'::uuid, 'encrypted-type-1', 'encrypted-title-1', 'encrypted-content-1', '["a", "b"]'::jsonb, 'encrypted-iv-1'),
       ('2b08c749-a996-44b6-9d12-9398b3789861'::uuid, '75ab36d3-3552-407b-b64b-0c56424fc479'::uuid, '5f6a97a3-52eb-44b2-983f-de9fc5bea7b8'::uuid, 'enc-type-2-will-be-deleted', 'enc-title-2', 'enc-content-2', '["a", "c"]'::jsonb, 'enc-iv-2');
