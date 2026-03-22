package com.payment.gateway.gateway.service.channel;

import com.payment.gateway.gateway.dto.request.BeneficiaryRequest;
import com.payment.gateway.gateway.dto.request.CustomerOnboardingRequest;
import com.payment.gateway.gateway.dto.request.PayoutRequest;
import com.payment.gateway.gateway.dto.request.VirtualAccountRequest;
import com.payment.gateway.gateway.dto.request.WebhookIngestRequest;
import com.payment.gateway.common.model.ChannelDescriptor;
import com.payment.gateway.gateway.model.GatewayExecutionResult;

public interface ChannelAdapter {

    String channelCode();

    ChannelDescriptor descriptor();

    GatewayExecutionResult onboardCustomer(CustomerOnboardingRequest request);

    GatewayExecutionResult createVirtualAccount(VirtualAccountRequest request);

    GatewayExecutionResult createBeneficiary(BeneficiaryRequest request);

    GatewayExecutionResult createPayout(PayoutRequest request);

    GatewayExecutionResult handleWebhook(WebhookIngestRequest request);
}
