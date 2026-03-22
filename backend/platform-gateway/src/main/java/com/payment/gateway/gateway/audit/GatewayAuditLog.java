package com.payment.gateway.gateway.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(
        catalog = "gateway_db",
        name = "gateway_audit_log",
        indexes = {
                @Index(name = "idx_gateway_audit_channel_operation", columnList = "channel_code, operation"),
                @Index(name = "idx_gateway_audit_processed_at", columnList = "processed_at")
        }
)
public class GatewayAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel_code", nullable = false, length = 32)
    private String channelCode;

    @Column(name = "operation", nullable = false, length = 64)
    private String operation;

    @Column(name = "request_id", length = 64)
    private String requestId;

    @Column(name = "success_flag", nullable = false)
    private boolean success;

    @Column(name = "message", length = 512)
    private String message;

    @Column(name = "downstream_endpoint", length = 256)
    private String downstreamEndpoint;

    @Lob
    @Column(name = "normalized_request_json", columnDefinition = "LONGTEXT")
    private String normalizedRequestJson;

    @Lob
    @Column(name = "translated_request_json", columnDefinition = "LONGTEXT")
    private String translatedRequestJson;

    @Lob
    @Column(name = "translated_response_json", columnDefinition = "LONGTEXT")
    private String translatedResponseJson;

    @Lob
    @Column(name = "result_json", columnDefinition = "LONGTEXT")
    private String resultJson;

    @Column(name = "processed_at", length = 64)
    private String processedAt;

    @Column(name = "duration_ms")
    private long durationMs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDownstreamEndpoint() {
        return downstreamEndpoint;
    }

    public void setDownstreamEndpoint(String downstreamEndpoint) {
        this.downstreamEndpoint = downstreamEndpoint;
    }

    public String getNormalizedRequestJson() {
        return normalizedRequestJson;
    }

    public void setNormalizedRequestJson(String normalizedRequestJson) {
        this.normalizedRequestJson = normalizedRequestJson;
    }

    public String getTranslatedRequestJson() {
        return translatedRequestJson;
    }

    public void setTranslatedRequestJson(String translatedRequestJson) {
        this.translatedRequestJson = translatedRequestJson;
    }

    public String getTranslatedResponseJson() {
        return translatedResponseJson;
    }

    public void setTranslatedResponseJson(String translatedResponseJson) {
        this.translatedResponseJson = translatedResponseJson;
    }

    public String getResultJson() {
        return resultJson;
    }

    public void setResultJson(String resultJson) {
        this.resultJson = resultJson;
    }

    public String getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(String processedAt) {
        this.processedAt = processedAt;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }
}
