package com.payment.gateway.gateway.model;

import java.time.Instant;
import java.util.Map;

public class GatewayExecutionResult {

    private final String channelCode;
    private final String operation;
    private final String requestId;
    private final boolean success;
    private final String message;
    private final String downstreamEndpoint;
    private final Object normalizedRequest;
    private final Map<String, Object> translatedRequest;
    private final Map<String, Object> translatedResponse;
    private final Map<String, Object> result;
    private final Instant processedAt;

    public GatewayExecutionResult(String channelCode, String operation, String requestId, boolean success,
                                  String message, String downstreamEndpoint, Object normalizedRequest,
                                  Map<String, Object> translatedRequest, Map<String, Object> translatedResponse,
                                  Map<String, Object> result, Instant processedAt) {
        this.channelCode = channelCode;
        this.operation = operation;
        this.requestId = requestId;
        this.success = success;
        this.message = message;
        this.downstreamEndpoint = downstreamEndpoint;
        this.normalizedRequest = normalizedRequest;
        this.translatedRequest = translatedRequest;
        this.translatedResponse = translatedResponse;
        this.result = result;
        this.processedAt = processedAt;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public String getOperation() {
        return operation;
    }

    public String getRequestId() {
        return requestId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getDownstreamEndpoint() {
        return downstreamEndpoint;
    }

    public Object getNormalizedRequest() {
        return normalizedRequest;
    }

    public Map<String, Object> getTranslatedRequest() {
        return translatedRequest;
    }

    public Map<String, Object> getTranslatedResponse() {
        return translatedResponse;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
