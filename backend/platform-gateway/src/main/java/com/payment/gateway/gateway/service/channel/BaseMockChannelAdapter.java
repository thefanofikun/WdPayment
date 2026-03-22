package com.payment.gateway.gateway.service.channel;

import java.util.Map;

import com.payment.gateway.gateway.model.GatewayExecutionResult;

public abstract class BaseMockChannelAdapter extends BaseChannelAdapterSupport {

    protected GatewayExecutionResult result(
            String operation,
            String downstreamEndpoint,
            Object normalizedRequest,
            Map<String, Object> translatedRequest,
            Map<String, Object> translatedResponse,
            Map<String, Object> result
    ) {
        return result(
                operation,
                downstreamEndpoint,
                normalizedRequest,
                translatedRequest,
                translatedResponse,
                result,
                true,
                "Accepted by mock downstream channel"
        );
    }
}
