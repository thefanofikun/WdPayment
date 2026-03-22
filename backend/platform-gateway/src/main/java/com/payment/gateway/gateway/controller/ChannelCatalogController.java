package com.payment.gateway.gateway.controller;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.payment.gateway.common.api.ApiResponse;
import com.payment.gateway.common.model.PayoutType;
import com.payment.gateway.gateway.service.ChannelRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class ChannelCatalogController {

    private final ChannelRegistry channelRegistry;

    public ChannelCatalogController(ChannelRegistry channelRegistry) {
        this.channelRegistry = channelRegistry;
    }

    @GetMapping("/channels")
    public ApiResponse<Map<String, Object>> listChannels() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("channels", channelRegistry.getSupportedChannels());
        response.put("payoutTypes", Arrays.asList(
                PayoutType.INTERNAL_TRANSFER,
                PayoutType.EXTERNAL_PAYOUT,
                PayoutType.POBO
        ));
        return ApiResponse.success(response);
    }
}
