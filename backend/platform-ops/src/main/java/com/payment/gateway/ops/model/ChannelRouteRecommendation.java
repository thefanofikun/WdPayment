package com.payment.gateway.ops.model;

public class ChannelRouteRecommendation {

    private final int rank;
    private final String operation;
    private final String channelCode;
    private final String channelName;
    private final double successRate;
    private final long averageLatencyMs;
    private final long totalCount;
    private final String recommendationReason;

    public ChannelRouteRecommendation(int rank, String operation, String channelCode, String channelName,
                                      double successRate, long averageLatencyMs, long totalCount,
                                      String recommendationReason) {
        this.rank = rank;
        this.operation = operation;
        this.channelCode = channelCode;
        this.channelName = channelName;
        this.successRate = successRate;
        this.averageLatencyMs = averageLatencyMs;
        this.totalCount = totalCount;
        this.recommendationReason = recommendationReason;
    }

    public int getRank() {
        return rank;
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

    public double getSuccessRate() {
        return successRate;
    }

    public long getAverageLatencyMs() {
        return averageLatencyMs;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public String getRecommendationReason() {
        return recommendationReason;
    }
}
