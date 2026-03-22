package com.payment.gateway.gateway.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.gateway.model.GatewayExecutionResult;
import org.springframework.stereotype.Service;

@Service
public class GatewayAuditLogService {

    private final GatewayAuditLogRepository gatewayAuditLogRepository;
    private final ObjectMapper objectMapper;

    public GatewayAuditLogService(GatewayAuditLogRepository gatewayAuditLogRepository, ObjectMapper objectMapper) {
        this.gatewayAuditLogRepository = gatewayAuditLogRepository;
        this.objectMapper = objectMapper;
    }

    public void recordResult(GatewayExecutionResult result, long durationMs) {
        GatewayAuditLog auditLog = new GatewayAuditLog();
        auditLog.setChannelCode(result.getChannelCode());
        auditLog.setOperation(result.getOperation());
        auditLog.setRequestId(result.getRequestId());
        auditLog.setSuccess(result.isSuccess());
        auditLog.setMessage(result.getMessage());
        auditLog.setDownstreamEndpoint(result.getDownstreamEndpoint());
        auditLog.setNormalizedRequestJson(toJson(result.getNormalizedRequest()));
        auditLog.setTranslatedRequestJson(toJson(result.getTranslatedRequest()));
        auditLog.setTranslatedResponseJson(toJson(result.getTranslatedResponse()));
        auditLog.setResultJson(toJson(result.getResult()));
        auditLog.setProcessedAt(result.getProcessedAt() == null ? null : result.getProcessedAt().toString());
        auditLog.setDurationMs(durationMs);
        gatewayAuditLogRepository.save(auditLog);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{\"serializationError\":\"" + ex.getMessage().replace("\"", "'") + "\"}";
        }
    }
}
