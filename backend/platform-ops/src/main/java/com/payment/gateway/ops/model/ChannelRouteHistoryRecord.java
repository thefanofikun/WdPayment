package com.payment.gateway.ops.model;

public class ChannelRouteHistoryRecord {

    private final Long id;
    private final String operation;
    private final String channelCode;
    private final String channelName;
    private final int routeRank;
    private final String scoreReason;
    private final double successRate;
    private final long averageLatencyMs;
    private final long totalCount;
    private final String createdAt;

    public ChannelRouteHistoryRecord(Long id, String operation, String channelCode, String channelName,
                                     int routeRank, String scoreReason, double successRate,
                                     long averageLatencyMs, long totalCount, String createdAt) {
        this.id = id;
        this.operation = operation;
        this.channelCode = channelCode;
        this.channelName = channelName;
        this.routeRank = routeRank;
        this.scoreReason = scoreReason;
        this.successRate = successRate;
        this.averageLatencyMs = averageLatencyMs;
        this.totalCount = totalCount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getOperation() {
        return operation;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public String getChannelName() {
        return channelName;
    }

    public int getRouteRank() {
        return routeRank;
    }

    public String getScoreReason() {
        return scoreReason;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public long getAverageLatencyMs() {
        return averageLatencyMs;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
