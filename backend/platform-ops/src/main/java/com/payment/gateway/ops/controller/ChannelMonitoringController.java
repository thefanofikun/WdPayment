package com.payment.gateway.ops.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import com.payment.gateway.common.api.ApiResponse;
import com.payment.gateway.ops.service.ChannelMonitoringService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitoring")
public class ChannelMonitoringController {

    private final ChannelMonitoringService channelMonitoringService;

    public ChannelMonitoringController(ChannelMonitoringService channelMonitoringService) {
        this.channelMonitoringService = channelMonitoringService;
    }

    @GetMapping("/channels")
    public ApiResponse<Map<String, Object>> listChannelMonitoring() {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("metrics", channelMonitoringService.getSnapshots());
        response.put("monitoredSignals", new String[] {
                "successRate",
                "failureCount",
                "averageLatencyMs",
                "lastStatus",
                "lastMessage",
                "lastUpdatedAt"
        });
        return ApiResponse.success(response);
    }
}
