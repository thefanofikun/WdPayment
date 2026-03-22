package com.payment.gateway.gateway.service.channel;

import java.util.Arrays;
import java.util.Map;

import com.payment.gateway.gateway.dto.request.BeneficiaryRequest;
import com.payment.gateway.gateway.dto.request.CustomerOnboardingRequest;
import com.payment.gateway.gateway.dto.request.PayoutRequest;
import com.payment.gateway.gateway.dto.request.VirtualAccountRequest;
import com.payment.gateway.gateway.dto.request.WebhookIngestRequest;
import com.payment.gateway.common.model.ChannelDescriptor;
import com.payment.gateway.gateway.model.GatewayExecutionResult;
import org.springframework.stereotype.Component;

@Component
public class HarborSwitchChannelAdapter extends BaseMockChannelAdapter {

    @Override
    public String channelCode() {
        return "HARBOR_SWITCH";
    }

    @Override
    public ChannelDescriptor descriptor() {
        return new ChannelDescriptor(
                channelCode(),
                "Harbor Switch",
                Arrays.asList("GB", "EU", "US"),
                Arrays.asList("CUSTOMER_ONBOARDING", "VIRTUAL_ACCOUNT", "BENEFICIARY", "PAYOUT", "WEBHOOK"),
                "Mock adapter with flatter payloads and alternate field names to exercise translation."
        );
    }

    @Override
    public GatewayExecutionResult onboardCustomer(CustomerOnboardingRequest request) {
        Map<String, Object> translatedRequest = orderedMap(
                "merchant_no", request.getMerchantId(),
                "merchant_customer_no", request.getCustomerReference(),
                "company_name", request.getLegalName(),
                "alias", request.getShortName(),
                "entity_type", request.getBusinessType(),
                "domicile", request.getCountry(),
                "registration_id", request.getRegistrationNumber(),
                "email_address", request.getContactEmail(),
                "mobile_number", request.getContactPhone(),
                "base_currency", request.getSettlementCurrency()
        );

        Map<String, Object> translatedResponse = orderedMap(
                "result_code", "0000",
                "partner_uid", generateId("HBR-CUS"),
                "compliance_state", "VERIFIED"
        );

        return result(
                "CUSTOMER_ONBOARDING",
                "/api/v2/partners",
                request,
                translatedRequest,
                translatedResponse,
                orderedMap(
                        "customerId", translatedResponse.get("partner_uid"),
                        "onboardingStatus", translatedResponse.get("compliance_state")
                )
        );
    }

    @Override
    public GatewayExecutionResult createVirtualAccount(VirtualAccountRequest request) {
        Map<String, Object> translatedRequest = orderedMap(
                "merchant_no", request.getMerchantId(),
                "partner_uid", request.getCustomerReference(),
                "account_alias", request.getVirtualAccountReference(),
                "display_name", request.getAccountName(),
                "currency", request.getCurrency(),
                "country_iso", request.getCountry(),
                "bank_route", request.getBankCode(),
                "usage", request.getPurpose()
        );

        Map<String, Object> translatedResponse = orderedMap(
                "result_code", "0000",
                "wallet_id", generateId("HBR-VA"),
                "va_number", "775544221100",
                "state", "ISSUED"
        );

        return result(
                "VIRTUAL_ACCOUNT",
                "/api/v2/wallets/virtual",
                request,
                translatedRequest,
                translatedResponse,
                orderedMap(
                        "virtualAccountId", translatedResponse.get("wallet_id"),
                        "accountNumber", translatedResponse.get("va_number"),
                        "status", translatedResponse.get("state")
                )
        );
    }

    @Override
    public GatewayExecutionResult createBeneficiary(BeneficiaryRequest request) {
        Map<String, Object> translatedRequest = orderedMap(
                "merchant_no", request.getMerchantId(),
                "partner_uid", request.getCustomerReference(),
                "counterparty_no", request.getBeneficiaryReference(),
                "counterparty_name", request.getBeneficiaryName(),
                "counterparty_type", request.getBeneficiaryType(),
                "bank_country_iso", request.getBankCountry(),
                "bank_routing_code", request.getBankCode(),
                "bank_account", request.getAccountNumber(),
                "iban_code", request.getIban(),
                "swift_bic", request.getSwiftCode(),
                "currency", request.getCurrency()
        );

        Map<String, Object> translatedResponse = orderedMap(
                "result_code", "0000",
                "counterparty_uid", generateId("HBR-BEN"),
                "state", "READY"
        );

        return result(
                "BENEFICIARY",
                "/api/v2/counterparties",
                request,
                translatedRequest,
                translatedResponse,
                orderedMap(
                        "beneficiaryId", translatedResponse.get("counterparty_uid"),
                        "status", translatedResponse.get("state")
                )
        );
    }

    @Override
    public GatewayExecutionResult createPayout(PayoutRequest request) {
        Map<String, Object> translatedRequest = orderedMap(
                "merchant_no", request.getMerchantId(),
                "instruction_no", request.getPayoutReference(),
                "instruction_type", request.getPayoutType().name(),
                "source_wallet", request.getSourceAccountReference(),
                "counterparty_no", request.getBeneficiaryReference(),
                "amount_value", request.getAmount(),
                "amount_ccy", request.getCurrency(),
                "description", request.getNarrative(),
                "purpose", request.getPurposeCode(),
                "requested_date", request.getValueDate()
        );

        Map<String, Object> translatedResponse = orderedMap(
                "result_code", "0000",
                "instruction_uid", generateId("HBR-PAY"),
                "state", "SUBMITTED"
        );

        return result(
                "PAYOUT",
                "/api/v2/transfers",
                request,
                translatedRequest,
                translatedResponse,
                orderedMap(
                        "paymentId", translatedResponse.get("instruction_uid"),
                        "status", translatedResponse.get("state")
                )
        );
    }

    @Override
    public GatewayExecutionResult handleWebhook(WebhookIngestRequest request) {
        Map<String, Object> translatedRequest = orderedMap(
                "event_uid", request.getEventId(),
                "event_name", request.getEventType(),
                "hmac", request.getSignature(),
                "body", request.getPayload()
        );

        Map<String, Object> translatedResponse = orderedMap(
                "result_code", "0000",
                "receipt_uid", generateId("HBR-EVT")
        );

        return result(
                "WEBHOOK",
                "/api/v2/events/consume",
                request,
                translatedRequest,
                translatedResponse,
                orderedMap(
                        "eventStatus", "ACKNOWLEDGED",
                        "eventReference", translatedResponse.get("receipt_uid")
                )
        );
    }
}
