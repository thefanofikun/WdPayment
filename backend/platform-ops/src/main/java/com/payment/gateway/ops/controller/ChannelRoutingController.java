package com.payment.gateway.ops.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import com.payment.gateway.common.api.ApiResponse;
import com.payment.gateway.ops.service.ChannelRoutingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routing")
public class ChannelRoutingController {

    private final ChannelRoutingService channelRoutingService;

    public ChannelRoutingController(ChannelRoutingService channelRoutingService) {
        this.channelRoutingService = channelRoutingService;
    }

    @GetMapping("/recommendations")
    public ApiResponse<Map<String, Object>> getRecommendations(
            @RequestParam(defaultValue = "PAYOUT") String operation) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("operation", operation);
        response.put("recommendations", channelRoutingService.recommendChannels(operation));
        response.put("sortBy", "successRate desc, averageLatencyMs asc");
        return ApiResponse.success(response);
    }

    @GetMapping("/history")
    public ApiResponse<Map<String, Object>> getRecentRouteHistory(
            @RequestParam(required = false) String operation) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("operation", operation);
        response.put("history", channelRoutingService.getRecentRouteHistory(operation));
        return ApiResponse.success(response);
    }
}
