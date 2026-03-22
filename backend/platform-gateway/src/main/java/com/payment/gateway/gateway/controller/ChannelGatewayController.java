package com.payment.gateway.gateway.controller;

import com.payment.gateway.common.api.ApiResponse;
import com.payment.gateway.gateway.dto.request.BeneficiaryRequest;
import com.payment.gateway.gateway.dto.request.CustomerOnboardingRequest;
import com.payment.gateway.gateway.dto.request.PayoutRequest;
import com.payment.gateway.gateway.dto.request.VirtualAccountRequest;
import com.payment.gateway.gateway.dto.request.WebhookIngestRequest;
import com.payment.gateway.gateway.model.GatewayExecutionResult;
import com.payment.gateway.gateway.service.ChannelGatewayService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gateway")
public class ChannelGatewayController {

    private final ChannelGatewayService channelGatewayService;

    public ChannelGatewayController(ChannelGatewayService channelGatewayService) {
        this.channelGatewayService = channelGatewayService;
    }

    @PostMapping("/customers/onboarding")
    public ApiResponse<GatewayExecutionResult> onboardCustomer(
            @Valid @RequestBody CustomerOnboardingRequest request) {
        return ApiResponse.success(channelGatewayService.onboardCustomer(request));
    }

    @PostMapping("/virtual-accounts")
    public ApiResponse<GatewayExecutionResult> createVirtualAccount(
            @Valid @RequestBody VirtualAccountRequest request) {
        return ApiResponse.success(channelGatewayService.createVirtualAccount(request));
    }

    @PostMapping("/beneficiaries")
    public ApiResponse<GatewayExecutionResult> createBeneficiary(
            @Valid @RequestBody BeneficiaryRequest request) {
        return ApiResponse.success(channelGatewayService.createBeneficiary(request));
    }

    @PostMapping("/payouts")
    public ApiResponse<GatewayExecutionResult> createPayout(
            @Valid @RequestBody PayoutRequest request) {
        return ApiResponse.success(channelGatewayService.createPayout(request));
    }

    @PostMapping("/webhooks/ingest")
    public ApiResponse<GatewayExecutionResult> ingestWebhook(
            @Valid @RequestBody WebhookIngestRequest request) {
        return ApiResponse.success(channelGatewayService.handleWebhook(request));
    }
}
