package com.payment.gateway.gateway.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GatewayAuditLogRepository extends JpaRepository<GatewayAuditLog, Long> {
}
