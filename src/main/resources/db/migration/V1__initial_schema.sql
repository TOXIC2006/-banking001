CREATE TABLE customer_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_number VARCHAR(20) NOT NULL,
    email VARCHAR(160) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    full_name VARCHAR(160) NOT NULL,
    phone VARCHAR(24) NOT NULL,
    address VARCHAR(500),
    role VARCHAR(20) NOT NULL,
    kyc_status VARCHAR(30) NOT NULL,
    kyc_document_type VARCHAR(40),
    kyc_document_number VARCHAR(100),
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    currency CHAR(3) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_account_number UNIQUE (account_number),
    CONSTRAINT uk_account_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE bank_transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reference VARCHAR(50) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    source_account_id BIGINT,
    destination_account_id BIGINT,
    transaction_type VARCHAR(30) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency CHAR(3) NOT NULL,
    description VARCHAR(255),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    completed_at TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_transaction_reference UNIQUE (reference),
    CONSTRAINT uk_transaction_idempotency UNIQUE (idempotency_key),
    CONSTRAINT fk_transaction_source FOREIGN KEY (source_account_id) REFERENCES customer_accounts(id),
    CONSTRAINT fk_transaction_destination FOREIGN KEY (destination_account_id) REFERENCES customer_accounts(id),
    INDEX idx_transaction_source_created (source_account_id, created_at),
    INDEX idx_transaction_destination_created (destination_account_id, created_at)
) ENGINE=InnoDB;

CREATE TABLE ledger_entries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    transaction_id BIGINT NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    balance_after DECIMAL(19, 2) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ledger_account FOREIGN KEY (account_id) REFERENCES customer_accounts(id),
    CONSTRAINT fk_ledger_transaction FOREIGN KEY (transaction_id) REFERENCES bank_transactions(id),
    INDEX idx_ledger_account_created (account_id, created_at)
) ENGINE=InnoDB;

CREATE TABLE fraud_cases (
    id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    transaction_reference VARCHAR(50),
    reason VARCHAR(255) NOT NULL,
    risk_score INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    resolved_at TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_fraud_customer FOREIGN KEY (customer_id) REFERENCES customer_accounts(id),
    INDEX idx_fraud_customer_created (customer_id, created_at)
) ENGINE=InnoDB;

CREATE TABLE support_tickets (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ticket_number VARCHAR(40) NOT NULL,
    customer_id BIGINT NOT NULL,
    transaction_reference VARCHAR(50),
    category VARCHAR(40) NOT NULL,
    subject VARCHAR(160) NOT NULL,
    details VARCHAR(1000) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_ticket_number UNIQUE (ticket_number),
    CONSTRAINT fk_ticket_customer FOREIGN KEY (customer_id) REFERENCES customer_accounts(id),
    INDEX idx_ticket_customer_created (customer_id, created_at)
) ENGINE=InnoDB;
