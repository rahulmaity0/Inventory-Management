INSERT INTO app_users (username, password, role) VALUES ('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN');

INSERT INTO inventory_state (id, locker_pure_gold) VALUES (1, 1167.269);

INSERT INTO clients (name, current_pure_balance) VALUES ('ABC Jewels', 291.571);
INSERT INTO clients (name, current_pure_balance) VALUES ('Shree Gold', 148.750);
INSERT INTO clients (name, current_pure_balance) VALUES ('Milan Ornaments', 92.410);

INSERT INTO gold_transactions (
    client_id,
    gross_weight,
    purity_percent,
    making_charge_percent,
    effective_purity_percent,
    pure_gold_equivalent,
    transaction_type,
    transaction_date,
    item_type,
    notes
) VALUES (
    1,
    321.450,
    91.80,
    2.00,
    93.80,
    301.521,
    'ISSUE',
    DATE '2026-03-01',
    'NECKLACE',
    'Jewellery issued to client using effective purity with making charge'
);

INSERT INTO gold_transactions (
    client_id,
    gross_weight,
    purity_percent,
    making_charge_percent,
    effective_purity_percent,
    pure_gold_equivalent,
    transaction_type,
    transaction_date,
    item_type,
    notes
) VALUES (
    2,
    160.000,
    91.50,
    1.50,
    93.00,
    148.750,
    'ISSUE',
    DATE '2026-03-05',
    'BANGLES',
    'Bangles issued to client'
);

INSERT INTO gold_transactions (
    client_id,
    gross_weight,
    purity_percent,
    making_charge_percent,
    effective_purity_percent,
    pure_gold_equivalent,
    transaction_type,
    transaction_date,
    item_type,
    notes
) VALUES (
    3,
    99.000,
    91.00,
    2.34,
    93.34,
    92.410,
    'ISSUE',
    DATE '2026-03-09',
    'NECKLACE',
    'Necklace issued to client'
);

INSERT INTO gold_transactions (
    client_id,
    gross_weight,
    purity_percent,
    making_charge_percent,
    effective_purity_percent,
    pure_gold_equivalent,
    transaction_type,
    transaction_date,
    item_type,
    notes
) VALUES (
    1,
    10.000,
    99.50,
    0.00,
    99.50,
    9.950,
    'RECEIPT',
    DATE '2026-03-10',
    'COIN_OR_BAR',
    'Client returned a 99.5 purity biscuit'
);
