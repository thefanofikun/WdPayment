package com.payment.gateway.ops.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        catalog = "ops_db",
        name = "channel_metric_snapshot",
        uniqueConstraints = @UniqueConstraint(name = "uk_channel_metric_snapshot", columnNames = {"channel_code", "operation_code"})
)
public class ChannelMetricSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel_code", nullable = false, length = 32)
    private String channelCode;

    @Column(name = "operation_code", nullable = false, length = 64)
    private String operationCode;

    @Column(name = "total_count", nullable = false)
    private long totalCount;

    @Column(name = "success_count", nullable = false)
    private long successCount;

    @Column(name = "failure_count", nullable = false)
    private long failureCount;

    @Column(name = "success_rate", nullable = false, precision = 8, scale = 4)
    private BigDecimal successRate;

    @Column(name = "average_latency_ms", nullable = false)
    private long averageLatencyMs;

    @Column(name = "last_status", length = 32)
    private String lastStatus;

    @Column(name = "last_message", length = 512)
    private String lastMessage;

    @Column(name = "last_updated_at", length = 64)
    private String lastUpdatedAt;

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

    public String getOperationCode() {
        return operationCode;
    }

    public void setOperationCode(String operationCode) {
        this.operationCode = operationCode;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(long successCount) {
        this.successCount = successCount;
    }

    public long getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(long failureCount) {
        this.failureCount = failureCount;
    }

    public BigDecimal getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(BigDecimal successRate) {
        this.successRate = successRate;
    }

    public long getAverageLatencyMs() {
        return averageLatencyMs;
    }

    public void setAverageLatencyMs(long averageLatencyMs) {
        this.averageLatencyMs = averageLatencyMs;
    }

    public String getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(String lastStatus) {
        this.lastStatus = lastStatus;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(String lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
