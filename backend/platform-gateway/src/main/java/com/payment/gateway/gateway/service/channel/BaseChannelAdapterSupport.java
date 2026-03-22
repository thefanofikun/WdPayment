package com.payment.gateway.gateway.service.channel;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.payment.gateway.gateway.model.GatewayExecutionResult;

public abstract class BaseChannelAdapterSupport implements ChannelAdapter {

    protected GatewayExecutionResult result(
            String operation,
            String downstreamEndpoint,
            Object normalizedRequest,
            Map<String, Object> translatedRequest,
            Map<String, Object> translatedResponse,
            Map<String, Object> result,
            boolean success,
            String message
    ) {
        return new GatewayExecutionResult(
                channelCode(),
                operation,
                generateId("REQ"),
                success,
                message,
                downstreamEndpoint,
                normalizedRequest,
                translatedRequest,
                translatedResponse,
                result,
                Instant.now()
        );
    }

    protected Map<String, Object> orderedMap(Object... entries) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (int i = 0; i < entries.length; i += 2) {
            map.put(String.valueOf(entries[i]), entries[i + 1]);
        }
        return map;
    }

    protected String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
