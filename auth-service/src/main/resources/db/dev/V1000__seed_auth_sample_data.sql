-- ROLE seed (RoleName enum allows only these values)
INSERT INTO roles (id, name, description, created_at) VALUES
    (1, 'ROLE_USER', 'Standard user with basic features', NOW() - INTERVAL '30 days'),
    (2, 'ROLE_ADMIN', 'Full system access, including user and configuration management', NOW() - INTERVAL '29 days'),
    (3, 'ROLE_MANAGER', 'Managerial access', NOW() - INTERVAL '28 days'),
    (4, 'ROLE_AUDITOR', 'Read-only access for auditing logs, reports, and transaction histories', NOW() - INTERVAL '27 days');

INSERT INTO users (
    id, email, password, first_name, last_name, phone_number,
    email_verified, phone_verified, account_status, account_locked,
    account_locked_until, failed_login_attempts, last_login_at, last_login_ip,
    password_changed_at, created_at
) VALUES
    (1, 'alice@finice.dev', '$2a$12$7Hmxup2KN3f0JEkCoby83Oprr2Qgr5eFiSUYD8EcFFHWFasi4Yt9a', 'Alice', 'Johnson', '+998901111001', true, true, 'ACTIVE', false, NULL, 0, NOW() - INTERVAL '1 day', '127.0.0.1', NOW() - INTERVAL '20 days', NOW() - INTERVAL '30 days'),
    (2, 'bob@finice.dev', '$2a$12$7Hmxup2KN3f0JEkCoby83Oprr2Qgr5eFiSUYD8EcFFHWFasi4Yt9a', 'Bob', 'Smith', '+998901111002', true, true, 'ACTIVE', false, NULL, 0, NOW() - INTERVAL '2 days', '127.0.0.1', NOW() - INTERVAL '21 days', NOW() - INTERVAL '29 days'),
    (3, 'carol@finice.dev', '$2a$12$7Hmxup2KN3f0JEkCoby83Oprr2Qgr5eFiSUYD8EcFFHWFasi4Yt9a', 'Carol', 'Lee', '+998901111003', true, true, 'ACTIVE', false, NULL, 0, NOW() - INTERVAL '3 days', '127.0.0.1', NOW() - INTERVAL '19 days', NOW() - INTERVAL '28 days'),
    (4, 'david@finice.dev', '$2a$12$7Hmxup2KN3f0JEkCoby83Oprr2Qgr5eFiSUYD8EcFFHWFasi4Yt9a', 'David', 'Brown', '+998901111004', true, false, 'PENDING_VERIFICATION', false, NULL, 0, NULL, NULL, NOW() - INTERVAL '10 days', NOW() - INTERVAL '27 days'),
    (5, 'emma@finice.dev', '$2a$12$7Hmxup2KN3f0JEkCoby83Oprr2Qgr5eFiSUYD8EcFFHWFasi4Yt9a', 'Emma', 'Wilson', '+998901111005', true, true, 'ACTIVE', false, NULL, 0, NOW() - INTERVAL '5 days', '127.0.0.1', NOW() - INTERVAL '18 days', NOW() - INTERVAL '26 days'),
    (6, 'frank@finice.dev', '$2a$12$7Hmxup2KN3f0JEkCoby83Oprr2Qgr5eFiSUYD8EcFFHWFasi4Yt9a', 'Frank', 'Taylor', '+998901111006', false, false, 'PENDING_VERIFICATION', false, NULL, 0, NULL, NULL, NOW() - INTERVAL '9 days', NOW() - INTERVAL '25 days'),
    (7, 'grace@finice.dev', '$2a$12$7Hmxup2KN3f0JEkCoby83Oprr2Qgr5eFiSUYD8EcFFHWFasi4Yt9a', 'Grace', 'Anderson', '+998901111007', true, true, 'SUSPENDED', false, NULL, 0, NOW() - INTERVAL '6 days', '127.0.0.1', NOW() - INTERVAL '25 days', NOW() - INTERVAL '24 days'),
    (8, 'henry@finice.dev', '$2a$12$7Hmxup2KN3f0JEkCoby83Oprr2Qgr5eFiSUYD8EcFFHWFasi4Yt9a', 'Henry', 'Thomas', '+998901111008', true, true, 'ACTIVE', false, NULL, 0, NOW() - INTERVAL '7 days', '127.0.0.1', NOW() - INTERVAL '22 days', NOW() - INTERVAL '23 days'),
    (9, 'isla@finice.dev', '$2a$12$7Hmxup2KN3f0JEkCoby83Oprr2Qgr5eFiSUYD8EcFFHWFasi4Yt9a', 'Isla', 'Martin', '+998901111009', true, true, 'INACTIVE', false, NULL, 0, NOW() - INTERVAL '8 days', '127.0.0.1', NOW() - INTERVAL '23 days', NOW() - INTERVAL '22 days'),
    (10, 'jack@finice.dev', '$2a$12$0Aekye4VTc.zsgLUmIy2CuNgA6.cGtH0RNkL41LYIVGrV39ifV6Ui', 'Jack', 'Clark', '+998901111010', true, true, 'ACTIVE', false, NULL, 0, NOW() - INTERVAL '4 days', '127.0.0.1', NOW() - INTERVAL '17 days', NOW() - INTERVAL '21 days');

-- Password: Finice_2026
-- Password: Admin_2026

-- Role assignments for all sample users
INSERT INTO users_roles (user_id, roles_id) VALUES
    (1, 1),
    (2, 1),
    (3, 1),
    (4, 1),
    (5, 1),
    (6, 1),
    (7, 1),
    (8, 1),
    (9, 4),
    (10, 2),
    (10, 3);

INSERT INTO email_tokens (
    id, token, token_type, user_id, expires_at, used_at, created_at
) VALUES
    (1, 'verify-token-u1', 'EMAIL_VERIFICATION', 1, NOW() + INTERVAL '1 day', NOW() - INTERVAL '29 days', NOW() - INTERVAL '30 days'),
    (2, 'verify-token-u2', 'EMAIL_VERIFICATION', 2, NOW() + INTERVAL '1 day', NOW() - INTERVAL '28 days', NOW() - INTERVAL '29 days'),
    (3, 'verify-token-u3', 'EMAIL_VERIFICATION', 3, NOW() + INTERVAL '1 day', NOW() - INTERVAL '27 days', NOW() - INTERVAL '28 days'),
    (4, 'verify-token-u4', 'EMAIL_VERIFICATION', 4, NOW() + INTERVAL '1 day', NULL, NOW() - INTERVAL '7 days'),
    (5, 'reset-token-u5', 'PASSWORD_RESET', 5, NOW() + INTERVAL '12 hours', NULL, NOW() - INTERVAL '2 days'),
    (6, 'reset-token-u6', 'PASSWORD_RESET', 6, NOW() - INTERVAL '1 hour', NULL, NOW() - INTERVAL '3 days'),
    (7, 'verify-token-u7', 'EMAIL_VERIFICATION', 7, NOW() + INTERVAL '1 day', NOW() - INTERVAL '24 days', NOW() - INTERVAL '24 days'),
    (8, 'email-change-u8', 'EMAIL_CHANGE_CONFIRMATION', 8, NOW() + INTERVAL '1 day', NULL, NOW() - INTERVAL '1 day'),
    (9, 'reset-token-u9', 'PASSWORD_RESET', 9, NOW() + INTERVAL '6 hours', NULL, NOW() - INTERVAL '12 hours'),
    (10, 'verify-token-u10', 'EMAIL_VERIFICATION', 10, NOW() + INTERVAL '1 day', NOW() - INTERVAL '20 days', NOW() - INTERVAL '21 days');


-- I also set sequences (setval) after explicit IDs so future inserts continue normally.

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('roles_id_seq', (SELECT MAX(id) FROM roles));
SELECT setval('email_tokens_id_seq', (SELECT MAX(id) FROM email_tokens));
