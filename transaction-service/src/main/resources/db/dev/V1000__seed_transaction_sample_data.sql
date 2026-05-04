INSERT INTO transactions (
    id, transaction_id, idempotency_key, reference, network_reference,
    transaction_type, source_account_id, destination_account_id,
    sender_iban, receiver_iban, receiver_name, transfer_scope,
    amount, currency, status, failure_reason, description,
    created_at, updated_at, completed_at, created_by
) VALUES
    (
        1, 'TXN-20260420-A3F8C2E1D4B7', 'f5dd9621-dfd0-4a5d-8410-a95445295a01', 'REF-20260420-X7K2P9', NULL,
        'TRANSFER', 1, 2,
        'UZ8510010000000000000001', 'UZ5810010000000000000002', NULL, 'INTERNAL',
        150.00, 'USD', 'COMPLETE', NULL, 'Move funds to emergency savings',
        NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days' + INTERVAL '1 minute', NOW() - INTERVAL '14 days' + INTERVAL '1 minute', 1
    ),
    (
        2, 'TXN-20260421-B9E7D4A1C2F6', '90fbef0c-b983-4d37-9b33-849f43cc2fa2', 'REF-20260421-M2N8Q1', NULL,
        'TRANSFER', 1, 6,
        'UZ8510010000000000000001', 'UZ4710010000000000000006', NULL, 'INTERNAL',
        500.00, 'USD', 'COMPLETE', NULL, 'Rent split transfer',
        NOW() - INTERVAL '13 days', NOW() - INTERVAL '13 days' + INTERVAL '2 minutes', NOW() - INTERVAL '13 days' + INTERVAL '2 minutes', 1
    ),
    (
        3, 'TXN-20260422-C1D2E3F4A5B6', '24dcf163-6d6e-4d56-a373-0fd2c936f983', 'REF-20260422-R8L4Z2', 'SWIFT-6A8B1C2D',
        'TRANSFER', 2, NULL,
        'UZ5810010000000000000002', 'DE44500105175407324931', 'Lena Kraus', 'EXTERNAL',
        220.00, 'USD', 'COMPLETE', NULL, 'External invoice payment',
        NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days' + INTERVAL '3 minutes', NOW() - INTERVAL '12 days' + INTERVAL '3 minutes', 1
    ),
    (
        4, 'TXN-20260423-D4A1B8C9E2F7', '34d0e15c-fa79-4d93-ae1a-08892f5641e4', 'REF-20260423-P5H9T3', NULL,
        'TRANSFER', 3, NULL,
        'UZ3110010000000000000003', 'GB33BUKB20201555555550', 'Noah Blake', 'EXTERNAL',
        75.00, 'USD', 'FAILED', 'Payment rejected by network simulator', 'Failed test transfer',
        NOW() - INTERVAL '11 days', NOW() - INTERVAL '11 days' + INTERVAL '2 minutes', NOW() - INTERVAL '11 days' + INTERVAL '2 minutes', 1
    ),
    (
        5, 'TXN-20260424-E8F2A1B3D4C5', '9cf868f8-f656-46ce-ad08-5282f8fb039e', 'REF-20260424-K3D1S7', NULL,
        'TRANSFER', 6, 8,
        'UZ4710010000000000000006', 'UZ9010010000000000000008', NULL, 'INTERNAL',
        320.00, 'USD', 'COMPLETE', NULL, 'Family support transfer',
        NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days' + INTERVAL '2 minutes', NOW() - INTERVAL '10 days' + INTERVAL '2 minutes', 5
    ),
    (
        6, 'TXN-20260425-F2C7A6E9D1B8', '2f66d4da-7e44-4063-93e8-cf610dbac2d6', 'REF-20260425-U2J6N4', 'SWIFT-9D7A3B1E',
        'TRANSFER', 7, NULL,
        'UZ2010010000000000000007', 'FR7630006000011234567890189', 'Ava Martin', 'EXTERNAL',
        140.00, 'USD', 'COMPLETE', NULL, 'Subscription payment',
        NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days' + INTERVAL '4 minutes', NOW() - INTERVAL '9 days' + INTERVAL '4 minutes', 5
    ),
    (
        7, 'TXN-20260426-A7C4E1D9B2F3', 'ba19f8f1-70c5-4c4e-971f-8af7a67a2f4c', 'REF-20260426-W5Q8M1', NULL,
        'TRANSFER', 6, 1,
        'UZ4710010000000000000006', 'UZ8510010000000000000001', NULL, 'INTERNAL',
        60.00, 'USD', 'CANCELLED', 'Cancelled by user', 'Cancelled transfer attempt',
        NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days' + INTERVAL '1 minute', NOW() - INTERVAL '8 days' + INTERVAL '1 minute', 5
    ),
    (
        8, 'TXN-20260427-B1D3F5A7C9E2', '35971e68-c4f0-4a26-ac1a-f35eb97574a5', 'REF-20260427-H9R3V2', NULL,
        'TRANSFER', 8, 9,
        'UZ9010010000000000000008', 'UZ6310010000000000000009', NULL, 'INTERNAL',
        410.00, 'USD', 'COMPLETE', NULL, 'Move to travel savings',
        NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days' + INTERVAL '2 minutes', NOW() - INTERVAL '7 days' + INTERVAL '2 minutes', 8
    ),
    (
        9, 'TXN-20260428-C5E7B9A1D3F4', '4f4135de-94a9-4288-b4af-f46ea3fd6944', 'REF-20260428-Z4P6L8', NULL,
        'TRANSFER', 8, NULL,
        'UZ9010010000000000000008', 'CH93007620116238529570', 'Mila Novak', 'EXTERNAL',
        95.00, 'USD', 'FAILED', 'Payment rejected by network simulator', 'External test payment',
        NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days' + INTERVAL '3 minutes', NOW() - INTERVAL '6 days' + INTERVAL '3 minutes', 8
    ),
    (
        10, 'TXN-20260429-D9A2C4E6F8B1', '8d5b8dbb-79b8-4530-a939-a23511fe9ce5', 'REF-20260429-N1T7K5', NULL,
        'TRANSFER', 9, 2,
        'UZ6310010000000000000009', 'UZ5810010000000000000002', NULL, 'INTERNAL',
        55.00, 'USD', 'PENDING', NULL, 'Pending shared expense transfer',
        NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', NULL, 8
    ),
    (
        11, 'TXN-20260501-E3B5D7F9A1C4', 'a7c7d5b1-1202-4f36-90ad-7d34bcf2f251', 'REF-20260501-Q2C8J4', 'SWIFT-2C7E4F9A',
        'TRANSFER', 1, NULL,
        'UZ8510010000000000000001', 'IT60X0542811101000000123456', 'Marco Bellini', 'EXTERNAL',
        180.00, 'USD', 'COMPLETE', NULL, 'Vendor payment',
        NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '3 minutes', NOW() - INTERVAL '3 days' + INTERVAL '3 minutes', 1
    ),
    (
        12, 'TXN-20260502-F6A8C1E3D5B7', '3e5617fd-a632-4265-b7f1-c2389aa0f526', 'REF-20260502-B6M2R9', NULL,
        'TRANSFER', 6, 3,
        'UZ4710010000000000000006', 'UZ3110010000000000000003', NULL, 'INTERNAL',
        205.00, 'USD', 'COMPLETE', NULL, 'Transfer for savings goal',
        NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '2 minutes', NOW() - INTERVAL '2 days' + INTERVAL '2 minutes', 5
    );

SELECT setval('transactions_id_seq', (SELECT MAX(id) FROM transactions));
