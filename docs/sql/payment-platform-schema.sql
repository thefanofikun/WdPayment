CREATE DATABASE IF NOT EXISTS payment_platform
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE payment_platform;

CREATE TABLE IF NOT EXISTS gateway_channel_config (
  id BIGINT NOT NULL AUTO_INCREMENT,
  channel_code VARCHAR(64) NOT NULL,
  channel_name VARCHAR(128) NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  priority_weight INT NOT NULL DEFAULT 100,
  supported_operations VARCHAR(512) NOT NULL,
  settlement_regions VARCHAR(512) DEFAULT NULL,
  auth_config_json JSON DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_gateway_channel_code (channel_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS gateway_channel_metric_snapshot (
  id BIGINT NOT NULL AUTO_INCREMENT,
  channel_code VARCHAR(64) NOT NULL,
  operation_code VARCHAR(64) NOT NULL,
  total_count BIGINT NOT NULL DEFAULT 0,
  success_count BIGINT NOT NULL DEFAULT 0,
  failure_count BIGINT NOT NULL DEFAULT 0,
  success_rate DECIMAL(8,4) NOT NULL DEFAULT 0,
  average_latency_ms BIGINT NOT NULL DEFAULT 0,
  last_status VARCHAR(32) DEFAULT NULL,
  last_message VARCHAR(512) DEFAULT NULL,
  last_updated_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_gateway_metric_channel_operation (channel_code, operation_code),
  KEY idx_gateway_metric_operation (operation_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS gateway_request_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  request_id VARCHAR(64) NOT NULL,
  channel_code VARCHAR(64) NOT NULL,
  operation_code VARCHAR(64) NOT NULL,
  merchant_id VARCHAR(64) DEFAULT NULL,
  business_reference VARCHAR(128) DEFAULT NULL,
  success_flag TINYINT(1) NOT NULL DEFAULT 0,
  latency_ms BIGINT NOT NULL DEFAULT 0,
  normalized_request_json JSON DEFAULT NULL,
  translated_request_json JSON DEFAULT NULL,
  translated_response_json JSON DEFAULT NULL,
  result_json JSON DEFAULT NULL,
  error_message VARCHAR(1000) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_gateway_request_id (request_id),
  KEY idx_gateway_request_channel_operation (channel_code, operation_code),
  KEY idx_gateway_request_merchant_reference (merchant_id, business_reference)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_order (
  id BIGINT NOT NULL AUTO_INCREMENT,
  payment_no VARCHAR(64) NOT NULL,
  merchant_id VARCHAR(64) NOT NULL,
  merchant_name VARCHAR(128) NOT NULL,
  customer_reference VARCHAR(64) NOT NULL,
  customer_name VARCHAR(128) NOT NULL,
  direction_code VARCHAR(32) NOT NULL,
  payment_method VARCHAR(32) NOT NULL,
  status_code VARCHAR(32) NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  currency VARCHAR(16) NOT NULL,
  business_reference VARCHAR(128) NOT NULL,
  idempotency_key VARCHAR(128) NOT NULL,
  source_account_reference VARCHAR(64) DEFAULT NULL,
  beneficiary_reference VARCHAR(64) DEFAULT NULL,
  beneficiary_name VARCHAR(128) DEFAULT NULL,
  requested_channel_code VARCHAR(64) DEFAULT NULL,
  routed_channel_code VARCHAR(64) DEFAULT NULL,
  gateway_operation VARCHAR(64) DEFAULT NULL,
  gateway_request_id VARCHAR(64) DEFAULT NULL,
  gateway_message VARCHAR(512) DEFAULT NULL,
  narrative VARCHAR(255) DEFAULT NULL,
  purpose_code VARCHAR(64) DEFAULT NULL,
  crm_case_id VARCHAR(64) DEFAULT NULL,
  sales_owner VARCHAR(128) DEFAULT NULL,
  relationship_manager VARCHAR(128) DEFAULT NULL,
  version_no INT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_payment_order_payment_no (payment_no),
  UNIQUE KEY uk_payment_order_merchant_idempotency (merchant_id, idempotency_key),
  KEY idx_payment_order_status (status_code),
  KEY idx_payment_order_merchant_created (merchant_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_approval_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  payment_order_id BIGINT NOT NULL,
  stage_code VARCHAR(32) NOT NULL,
  decision_code VARCHAR(32) NOT NULL,
  actor VARCHAR(128) NOT NULL,
  comment_text VARCHAR(512) DEFAULT NULL,
  acted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_payment_approval_order_stage (payment_order_id, stage_code),
  CONSTRAINT fk_payment_approval_order
    FOREIGN KEY (payment_order_id) REFERENCES payment_order(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_idempotency (
  id BIGINT NOT NULL AUTO_INCREMENT,
  merchant_id VARCHAR(64) NOT NULL,
  idempotency_key VARCHAR(128) NOT NULL,
  payment_order_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_payment_idempotency_merchant_key (merchant_id, idempotency_key),
  KEY idx_payment_idempotency_order (payment_order_id),
  CONSTRAINT fk_payment_idempotency_order
    FOREIGN KEY (payment_order_id) REFERENCES payment_order(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS crm_merchant_stub (
  id BIGINT NOT NULL AUTO_INCREMENT,
  merchant_id VARCHAR(64) NOT NULL,
  merchant_name VARCHAR(128) NOT NULL,
  relationship_manager VARCHAR(128) DEFAULT NULL,
  default_currency VARCHAR(16) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_crm_merchant_stub_merchant_id (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS crm_customer_stub (
  id BIGINT NOT NULL AUTO_INCREMENT,
  customer_reference VARCHAR(64) NOT NULL,
  customer_name VARCHAR(128) NOT NULL,
  merchant_id VARCHAR(64) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_crm_customer_stub_customer_reference (customer_reference),
  KEY idx_crm_customer_stub_merchant (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS crm_beneficiary_stub (
  id BIGINT NOT NULL AUTO_INCREMENT,
  beneficiary_reference VARCHAR(64) NOT NULL,
  beneficiary_name VARCHAR(128) NOT NULL,
  merchant_id VARCHAR(64) NOT NULL,
  bank_code VARCHAR(64) DEFAULT NULL,
  account_number VARCHAR(128) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_crm_beneficiary_stub_reference (beneficiary_reference),
  KEY idx_crm_beneficiary_stub_merchant (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS crm_source_account_stub (
  id BIGINT NOT NULL AUTO_INCREMENT,
  source_account_reference VARCHAR(64) NOT NULL,
  merchant_id VARCHAR(64) NOT NULL,
  currency VARCHAR(16) NOT NULL,
  account_name VARCHAR(128) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_crm_source_account_stub_reference (source_account_reference),
  KEY idx_crm_source_account_stub_merchant (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO gateway_channel_config (channel_code, channel_name, enabled, priority_weight, supported_operations, settlement_regions)
VALUES
  ('APEX_PAY', 'Apex Pay', 1, 100, 'CUSTOMER_ONBOARDING,VIRTUAL_ACCOUNT,BENEFICIARY,PAYOUT,WEBHOOK', 'SG,HK,AE'),
  ('HARBOR_SWITCH', 'Harbor Switch', 1, 90, 'CUSTOMER_ONBOARDING,VIRTUAL_ACCOUNT,BENEFICIARY,PAYOUT,WEBHOOK', 'GB,EU,US')
ON DUPLICATE KEY UPDATE
  channel_name = VALUES(channel_name),
  enabled = VALUES(enabled),
  priority_weight = VALUES(priority_weight),
  supported_operations = VALUES(supported_operations),
  settlement_regions = VALUES(settlement_regions);

INSERT INTO crm_merchant_stub (merchant_id, merchant_name, relationship_manager, default_currency)
VALUES
  ('MERCHANT-001', 'Northstar Treasury Ltd', 'Alice Wong', 'USD'),
  ('MERCHANT-002', 'Bluewave Commerce LLC', 'Daniel Tan', 'EUR')
ON DUPLICATE KEY UPDATE
  merchant_name = VALUES(merchant_name),
  relationship_manager = VALUES(relationship_manager),
  default_currency = VALUES(default_currency);

INSERT INTO crm_customer_stub (customer_reference, customer_name, merchant_id)
VALUES
  ('CUS-10001', 'Northstar Operating Account', 'MERCHANT-001'),
  ('CUS-10002', 'Northstar Client Funds', 'MERCHANT-001'),
  ('CUS-20001', 'Bluewave Collections', 'MERCHANT-002')
ON DUPLICATE KEY UPDATE
  customer_name = VALUES(customer_name),
  merchant_id = VALUES(merchant_id);

INSERT INTO crm_beneficiary_stub (beneficiary_reference, beneficiary_name, merchant_id, bank_code, account_number)
VALUES
  ('BEN-31001', 'Oceanic Supplies Pte Ltd', 'MERCHANT-001', '7339', '1234567890'),
  ('BEN-31002', 'Harbor Logistics Ltd', 'MERCHANT-001', '001', '9988776655'),
  ('BEN-41001', 'Euro Retail GmbH', 'MERCHANT-002', '50010517', 'DE12100100101234567895')
ON DUPLICATE KEY UPDATE
  beneficiary_name = VALUES(beneficiary_name),
  merchant_id = VALUES(merchant_id),
  bank_code = VALUES(bank_code),
  account_number = VALUES(account_number);

INSERT INTO crm_source_account_stub (source_account_reference, merchant_id, currency, account_name)
VALUES
  ('VA-90001', 'MERCHANT-001', 'USD', 'Northstar Client Funds'),
  ('SETTLE-USD-01', 'MERCHANT-001', 'USD', 'Northstar Settlement'),
  ('SETTLE-EUR-01', 'MERCHANT-002', 'EUR', 'Bluewave Settlement')
ON DUPLICATE KEY UPDATE
  merchant_id = VALUES(merchant_id),
  currency = VALUES(currency),
  account_name = VALUES(account_name);
