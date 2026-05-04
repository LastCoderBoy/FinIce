-- Sample accounts

INSERT INTO accounts (
    id, iban, user_id, account_type, account_nick_name, currency,
    balance, available_balance, hold_amount, status,
    daily_withdrawal_limit, daily_transfer_limit, interest_rate,
    created_at, updated_at, last_transaction_at
) VALUES
    (1, 'UZ8510010000000000000001', 1, 'CURRENT', 'Main Current', 'USD',
     25000.00, 25000.00, 0.00, 'ACTIVE',
     50000.00, 500000.00, 0.00,
     NOW() - INTERVAL '20 days', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),

    (2, 'UZ5810010000000000000002', 1, 'SAVINGS', 'Emergency Savings', 'USD',
     8200.00, 8200.00, 0.00, 'ACTIVE',
     5000.00, 20000.00, 3.50,
     NOW() - INTERVAL '19 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),

    (3, 'UZ3110010000000000000003', 1, 'SAVINGS', 'Vacation Savings', 'USD',
     4300.00, 4300.00, 0.00, 'ACTIVE',
     5000.00, 20000.00, 3.50,
     NOW() - INTERVAL '18 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),

    (4, 'UZ0410010000000000000004', 2, 'CURRENT', 'Salary Current', 'USD',
     10250.00, 10250.00, 0.00, 'ACTIVE',
     50000.00, 500000.00, 0.00,
     NOW() - INTERVAL '17 days', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),

    (5, 'UZ7410010000000000000005', 3, 'SAVINGS', 'Future Savings', 'EUR',
     1200.00, 1200.00, 0.00, 'ACTIVE',
     5000.00, 20000.00, 3.50,
     NOW() - INTERVAL '16 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),

    (6, 'UZ4710010000000000000006', 5, 'CURRENT', 'Daily Current', 'USD',
     8700.00, 8700.00, 0.00, 'ACTIVE',
     50000.00, 500000.00, 0.00,
     NOW() - INTERVAL '15 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),

    (7, 'UZ2010010000000000000007', 5, 'SAVINGS', 'Reserve Savings', 'USD',
     2100.00, 2100.00, 0.00, 'ACTIVE',
     5000.00, 20000.00, 3.50,
     NOW() - INTERVAL '14 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),

    (8, 'UZ9010010000000000000008', 8, 'CURRENT', 'Primary Current', 'USD',
     15400.00, 15400.00, 0.00, 'ACTIVE',
     50000.00, 500000.00, 0.00,
     NOW() - INTERVAL '13 days', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),

    (9, 'UZ6310010000000000000009', 8, 'SAVINGS', 'Travel Savings', 'USD',
     3600.00, 3600.00, 0.00, 'ACTIVE',
     5000.00, 20000.00, 3.50,
     NOW() - INTERVAL '12 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');

SELECT setval('accounts_id_seq', (SELECT MAX(id) FROM accounts));
