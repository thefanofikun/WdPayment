CREATE DATABASE IF NOT EXISTS gateway_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS payment_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS ops_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS gateway_db.gateway_audit_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  channel_code VARCHAR(32) NOT NULL,
  operation VARCHAR(64) NOT NULL,
  request_id VARCHAR(64) DEFAULT NULL,
  success_flag BIT(1) NOT NULL,
  message VARCHAR(512) DEFAULT NULL,
  downstream_endpoint VARCHAR(256) DEFAULT NULL,
  normalized_request_json LONGTEXT DEFAULT NULL,
  translated_request_json LONGTEXT DEFAULT NULL,
  translated_response_json LONGTEXT DEFAULT NULL,
  result_json LONGTEXT DEFAULT NULL,
  processed_at VARCHAR(64) DEFAULT NULL,
  duration_ms BIGINT DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_gateway_audit_channel_operation (channel_code, operation),
  KEY idx_gateway_audit_processed_at (processed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS gateway_db.gateway_channel_config (
  id BIGINT NOT NULL AUTO_INCREMENT,
  channel_code VARCHAR(32) NOT NULL,
  channel_name VARCHAR(128) NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  priority_weight INT NOT NULL DEFAULT 100,
  supported_operations VARCHAR(512) NOT NULL,
  settlement_regions VARCHAR(512) DEFAULT NULL,
  auth_config_json JSON DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_gateway_channel_config_code (channel_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS gateway_db.gateway_webhook_event_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  channel_code VARCHAR(32) NOT NULL,
  event_type VARCHAR(64) NOT NULL,
  event_id VARCHAR(128) DEFAULT NULL,
  accepted TINYINT(1) NOT NULL DEFAULT 1,
  payload_json LONGTEXT DEFAULT NULL,
  normalized_result_json LONGTEXT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_gateway_webhook_channel_event (channel_code, event_type),
  KEY idx_gateway_webhook_event_id (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_db.payment_order (
  id BIGINT NOT NULL AUTO_INCREMENT,
  payment_no VARCHAR(64) NOT NULL,
  merchant_id VARCHAR(64) NOT NULL,
  merchant_name VARCHAR(256) NOT NULL,
  customer_reference VARCHAR(64) NOT NULL,
  customer_name VARCHAR(256) NOT NULL,
  direction VARCHAR(32) NOT NULL,
  payment_method VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  currency VARCHAR(16) NOT NULL,
  source_account_reference VARCHAR(64) DEFAULT NULL,
  beneficiary_reference VARCHAR(64) DEFAULT NULL,
  beneficiary_name VARCHAR(256) DEFAULT NULL,
  requested_channel_code VARCHAR(32) DEFAULT NULL,
  routed_channel_code VARCHAR(32) DEFAULT NULL,
  gateway_operation VARCHAR(64) DEFAULT NULL,
  gateway_request_id VARCHAR(64) DEFAULT NULL,
  gateway_message VARCHAR(512) DEFAULT NULL,
  business_reference VARCHAR(64) DEFAULT NULL,
  idempotency_key VARCHAR(128) NOT NULL,
  narrative VARCHAR(512) DEFAULT NULL,
  purpose_code VARCHAR(64) DEFAULT NULL,
  crm_case_id VARCHAR(64) DEFAULT NULL,
  sales_owner VARCHAR(128) DEFAULT NULL,
  relationship_manager VARCHAR(128) DEFAULT NULL,
  version_no INT NOT NULL,
  created_at VARCHAR(64) NOT NULL,
  updated_at VARCHAR(64) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_payment_order_payment_no (payment_no),
  UNIQUE KEY uk_payment_order_merchant_idempotency (merchant_id, idempotency_key),
  KEY idx_payment_order_status (status),
  KEY idx_payment_order_merchant (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_db.payment_approval_record (
  payment_order_id BIGINT NOT NULL,
  approval_order INT NOT NULL,
  stage VARCHAR(32) NOT NULL,
  decision VARCHAR(32) NOT NULL,
  actor VARCHAR(128) NOT NULL,
  comment_text VARCHAR(512) DEFAULT NULL,
  acted_at VARCHAR(64) NOT NULL,
  PRIMARY KEY (payment_order_id, approval_order),
  CONSTRAINT fk_payment_approval_record_order
    FOREIGN KEY (payment_order_id) REFERENCES payment_db.payment_order(id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_db.payment_event_record (
  payment_order_id BIGINT NOT NULL,
  event_order INT NOT NULL,
  event_type VARCHAR(64) NOT NULL,
  actor VARCHAR(128) NOT NULL,
  comment_text VARCHAR(512) DEFAULT NULL,
  event_at VARCHAR(64) NOT NULL,
  PRIMARY KEY (payment_order_id, event_order),
  CONSTRAINT fk_payment_event_record_order
    FOREIGN KEY (payment_order_id) REFERENCES payment_db.payment_order(id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ops_db.channel_metric_snapshot (
  id BIGINT NOT NULL AUTO_INCREMENT,
  channel_code VARCHAR(32) NOT NULL,
  operation_code VARCHAR(64) NOT NULL,
  total_count BIGINT NOT NULL DEFAULT 0,
  success_count BIGINT NOT NULL DEFAULT 0,
  failure_count BIGINT NOT NULL DEFAULT 0,
  success_rate DECIMAL(8,4) NOT NULL DEFAULT 0,
  average_latency_ms BIGINT NOT NULL DEFAULT 0,
  last_status VARCHAR(32) DEFAULT NULL,
  last_message VARCHAR(512) DEFAULT NULL,
  last_updated_at VARCHAR(64) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_channel_metric_snapshot (channel_code, operation_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ops_db.channel_route_history (
  id BIGINT NOT NULL AUTO_INCREMENT,
  operation_code VARCHAR(64) NOT NULL,
  channel_code VARCHAR(32) NOT NULL,
  route_rank INT NOT NULL,
  score_reason VARCHAR(512) DEFAULT NULL,
  success_rate DECIMAL(8,4) DEFAULT NULL,
  average_latency_ms BIGINT DEFAULT NULL,
  total_count BIGINT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_channel_route_history_operation (operation_code),
  KEY idx_channel_route_history_channel (channel_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO gateway_db.gateway_channel_config (
  channel_code,
  channel_name,
  enabled,
  priority_weight,
  supported_operations,
  settlement_regions
) VALUES
  ('APEX_PAY', 'Apex Pay', 1, 100, 'CUSTOMER_ONBOARDING,VIRTUAL_ACCOUNT,BENEFICIARY,PAYOUT,WEBHOOK', 'SG,HK,AE'),
  ('HARBOR_SWITCH', 'Harbor Switch', 1, 90, 'CUSTOMER_ONBOARDING,VIRTUAL_ACCOUNT,BENEFICIARY,PAYOUT,WEBHOOK', 'GB,EU,US'),
  ('SGB', 'Singapore Gulf Bank', 1, 95, 'VIRTUAL_ACCOUNT,BENEFICIARY,PAYOUT,WEBHOOK', 'BH,SG,AE,US,EU')
ON DUPLICATE KEY UPDATE
  channel_name = VALUES(channel_name),
  enabled = VALUES(enabled),
  priority_weight = VALUES(priority_weight),
  supported_operations = VALUES(supported_operations),
  settlement_regions = VALUES(settlement_regions);
