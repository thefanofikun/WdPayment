package com.payment.gateway.ops.model;

public class ChannelMetricSnapshot {

    private final String channelCode;
    private final String operation;
    private final long totalCount;
    private final long successCount;
    private final long failureCount;
    private final double successRate;
    private final long averageLatencyMs;
    private final String lastStatus;
    private final String lastMessage;
    private final String lastUpdatedAt;

    public ChannelMetricSnapshot(String channelCode, String operation, long totalCount, long successCount,
                                 long failureCount, double successRate, long averageLatencyMs,
                                 String lastStatus, String lastMessage, String lastUpdatedAt) {
        this.channelCode = channelCode;
        this.operation = operation;
        this.totalCount = totalCount;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.successRate = successRate;
        this.averageLatencyMs = averageLatencyMs;
        this.lastStatus = lastStatus;
        this.lastMessage = lastMessage;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public String getOperation() {
        return operation;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public long getFailureCount() {
        return failureCount;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public long getAverageLatencyMs() {
        return averageLatencyMs;
    }

    public String getLastStatus() {
        return lastStatus;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastUpdatedAt() {
        return lastUpdatedAt;
    }
}
