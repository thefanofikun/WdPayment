package com.payment.gateway.gateway.service;

import java.util.function.Function;

import com.payment.gateway.common.channel.ChannelExecutionRecorder;
import com.payment.gateway.gateway.audit.GatewayAuditLogService;
import com.payment.gateway.gateway.dto.request.BeneficiaryRequest;
import com.payment.gateway.gateway.dto.request.CustomerOnboardingRequest;
import com.payment.gateway.gateway.dto.request.PayoutRequest;
import com.payment.gateway.gateway.dto.request.VirtualAccountRequest;
import com.payment.gateway.gateway.dto.request.WebhookIngestRequest;
import com.payment.gateway.gateway.model.GatewayExecutionResult;
import com.payment.gateway.gateway.service.channel.ChannelAdapter;
import org.springframework.stereotype.Service;

@Service
public class ChannelGatewayService {

    private final ChannelRegistry channelRegistry;
    private final ChannelExecutionRecorder channelExecutionRecorder;
    private final GatewayAuditLogService gatewayAuditLogService;

    public ChannelGatewayService(ChannelRegistry channelRegistry,
                                 ChannelExecutionRecorder channelExecutionRecorder,
                                 GatewayAuditLogService gatewayAuditLogService) {
        this.channelRegistry = channelRegistry;
        this.channelExecutionRecorder = channelExecutionRecorder;
        this.gatewayAuditLogService = gatewayAuditLogService;
    }

    public GatewayExecutionResult onboardCustomer(CustomerOnboardingRequest request) {
        return execute(request.getChannelCode(), "CUSTOMER_ONBOARDING", new Function<ChannelAdapter, GatewayExecutionResult>() {
            @Override
            public GatewayExecutionResult apply(ChannelAdapter adapter) {
                return adapter.onboardCustomer(request);
            }
        });
    }

    public GatewayExecutionResult createVirtualAccount(VirtualAccountRequest request) {
        return execute(request.getChannelCode(), "VIRTUAL_ACCOUNT", new Function<ChannelAdapter, GatewayExecutionResult>() {
            @Override
            public GatewayExecutionResult apply(ChannelAdapter adapter) {
                return adapter.createVirtualAccount(request);
            }
        });
    }

    public GatewayExecutionResult createBeneficiary(BeneficiaryRequest request) {
        return execute(request.getChannelCode(), "BENEFICIARY", new Function<ChannelAdapter, GatewayExecutionResult>() {
            @Override
            public GatewayExecutionResult apply(ChannelAdapter adapter) {
                return adapter.createBeneficiary(request);
            }
        });
    }

    public GatewayExecutionResult createPayout(PayoutRequest request) {
        return execute(request.getChannelCode(), "PAYOUT", new Function<ChannelAdapter, GatewayExecutionResult>() {
            @Override
            public GatewayExecutionResult apply(ChannelAdapter adapter) {
                return adapter.createPayout(request);
            }
        });
    }

    public GatewayExecutionResult handleWebhook(WebhookIngestRequest request) {
        return execute(request.getChannelCode(), "WEBHOOK", new Function<ChannelAdapter, GatewayExecutionResult>() {
            @Override
            public GatewayExecutionResult apply(ChannelAdapter adapter) {
                return adapter.handleWebhook(request);
            }
        });
    }

    private GatewayExecutionResult execute(String channelCode, String operation,
                                           Function<ChannelAdapter, GatewayExecutionResult> action) {
        ChannelAdapter adapter = channelRegistry.resolve(channelCode);
        long startedAt = System.currentTimeMillis();
        try {
            GatewayExecutionResult result = action.apply(adapter);
            long latencyMs = System.currentTimeMillis() - startedAt;
            channelExecutionRecorder.recordExecution(channelCode, operation, result.isSuccess(), latencyMs, result.getMessage());
            gatewayAuditLogService.recordResult(result, latencyMs);
            return result;
        } catch (RuntimeException ex) {
            long latencyMs = System.currentTimeMillis() - startedAt;
            channelExecutionRecorder.recordExecution(channelCode, operation, false, latencyMs, ex.getMessage());
            throw ex;
        }
    }
}
