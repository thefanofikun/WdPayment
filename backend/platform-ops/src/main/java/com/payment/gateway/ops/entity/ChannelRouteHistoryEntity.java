package com.payment.gateway.ops.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(catalog = "ops_db", name = "channel_route_history")
public class ChannelRouteHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_code", nullable = false, length = 64)
    private String operationCode;

    @Column(name = "channel_code", nullable = false, length = 32)
    private String channelCode;

    @Column(name = "route_rank", nullable = false)
    private int routeRank;

    @Column(name = "score_reason", length = 512)
    private String scoreReason;

    @Column(name = "success_rate", precision = 8, scale = 4)
    private BigDecimal successRate;

    @Column(name = "average_latency_ms")
    private Long averageLatencyMs;

    @Column(name = "total_count")
    private Long totalCount;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOperationCode() {
        return operationCode;
    }

    public void setOperationCode(String operationCode) {
        this.operationCode = operationCode;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public int getRouteRank() {
        return routeRank;
    }

    public void setRouteRank(int routeRank) {
        this.routeRank = routeRank;
    }

    public String getScoreReason() {
        return scoreReason;
    }

    public void setScoreReason(String scoreReason) {
        this.scoreReason = scoreReason;
    }

    public BigDecimal getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(BigDecimal successRate) {
        this.successRate = successRate;
    }

    public Long getAverageLatencyMs() {
        return averageLatencyMs;
    }

    public void setAverageLatencyMs(Long averageLatencyMs) {
        this.averageLatencyMs = averageLatencyMs;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
