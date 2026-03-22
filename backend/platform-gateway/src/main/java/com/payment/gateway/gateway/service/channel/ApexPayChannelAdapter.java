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
public class ApexPayChannelAdapter extends BaseMockChannelAdapter {

    @Override
    public String channelCode() {
        return "APEX_PAY";
    }

    @Override
    public ChannelDescriptor descriptor() {
        return new ChannelDescriptor(
                channelCode(),
                "Apex Pay",
                Arrays.asList("SG", "HK", "AE"),
                Arrays.asList("CUSTOMER_ONBOARDING", "VIRTUAL_ACCOUNT", "BENEFICIARY", "PAYOUT", "WEBHOOK"),
                "Mock adapter with nested payloads similar to bank aggregator style APIs."
        );
    }

    @Override
    public GatewayExecutionResult onboardCustomer(CustomerOnboardingRequest request) {
        Map<String, Object> translatedRequest = orderedMap(
                "client_id", request.getMerchantId(),
                "profile", orderedMap(
                        "customer_ref", request.getCustomerReference(),
                        "legal_name", request.getLegalName(),
                        "display_name", request.getShortName(),
                        "business_type", request.getBusinessType(),
                        "country", request.getCountry(),
                        "registration_no", request.getRegistrationNumber()
                ),
                "contact", orderedMap(
                        "email", request.getContactEmail(),
                        "phone", request.getContactPhone(),
                        "rm", request.getRelationshipManager()
                ),
                "settlement_ccy", request.getSettlementCurrency()
        );

        Map<String, Object> translatedResponse = orderedMap(
                "status", "APPROVED",
                "customer_id", generateId("APX-CUS"),
                "risk_band", "STANDARD"
        );

        return result(
                "CUSTOMER_ONBOARDING",
                "/partners/customers",
                request,
                translatedRequest,
                translatedResponse,
                orderedMap(
                        "customerId", translatedResponse.get("customer_id"),
                        "onboardingStatus", translatedResponse.get("status")
                )
        );
    }

    @Override
    public GatewayExecutionResult createVirtualAccount(VirtualAccountRequest request) {
        Map<String, Object> translatedRequest = orderedMap(
                "merchant_ref", request.getMerchantId(),
                "customer_ref", request.getCustomerReference(),
                "va_ref", request.getVirtualAccountReference(),
                "account_name", request.getAccountName(),
                "account_currency", request.getCurrency(),
                "country", request.getCountry(),
                "routing_bank", request.getBankCode(),
                "purpose", request.getPurpose()
        );

        Map<String, Object> translatedResponse = orderedMap(
                "status", "ACTIVE",
                "va_id", generateId("APX-VA"),
                "account_number", "998811223344",
                "bank_name", "Apex Clearing Bank"
        );

        return result(
                "VIRTUAL_ACCOUNT",
                "/accounts/virtual",
                request,
                translatedRequest,
                translatedResponse,
                orderedMap(
                        "virtualAccountId", translatedResponse.get("va_id"),
                        "accountNumber", translatedResponse.get("account_number"),
                        "status", translatedResponse.get("status")
                )
        );
    }

    @Override
    public GatewayExecutionResult createBeneficiary(BeneficiaryRequest request) {
        Map<String, Object> translatedRequest = orderedMap(
                "merchant_ref", request.getMerchantId(),
                "owner_customer_ref", request.getCustomerReference(),
                "counterparty", orderedMap(
                        "beneficiary_ref", request.getBeneficiaryReference(),
                        "name", request.getBeneficiaryName(),
                        "type", request.getBeneficiaryType(),
                        "bank_country", request.getBankCountry(),
                        "bank_code", request.getBankCode(),
                        "account_number", request.getAccountNumber(),
                        "iban", request.getIban(),
                        "swift", request.getSwiftCode(),
                        "currency", request.getCurrency()
                )
        );

        Map<String, Object> translatedResponse = orderedMap(
                "status", "PENDING_REVIEW",
                "beneficiary_id", generateId("APX-BEN"),
                "screening_case", generateId("SCR")
        );

        return result(
                "BENEFICIARY",
                "/counterparties",
                request,
                translatedRequest,
                translatedResponse,
                orderedMap(
                        "beneficiaryId", translatedResponse.get("beneficiary_id"),
                        "status", translatedResponse.get("status")
                )
        );
    }

    @Override
    public GatewayExecutionResult createPayout(PayoutRequest request) {
        Map<String, Object> translatedRequest = orderedMap(
                "merchant_ref", request.getMerchantId(),
                "transfer_ref", request.getPayoutReference(),
                "transfer_type", request.getPayoutType().name(),
                "debit_account_ref", request.getSourceAccountReference(),
                "beneficiary_ref", request.getBeneficiaryReference(),
                "amount", orderedMap(
                        "value", request.getAmount(),
                        "currency", request.getCurrency()
                ),
                "narrative", request.getNarrative(),
                "purpose_code", request.getPurposeCode(),
                "value_date", request.getValueDate()
        );

        Map<String, Object> translatedResponse = orderedMap(
                "status", "QUEUED",
                "payment_id", generateId("APX-PAY"),
                "network_reference", generateId("NET")
        );

        return result(
                "PAYOUT",
                "/payments/outbound",
                request,
                translatedRequest,
                translatedResponse,
                orderedMap(
                        "paymentId", translatedResponse.get("payment_id"),
                        "status", translatedResponse.get("status"),
                        "networkReference", translatedResponse.get("network_reference")
                )
        );
    }

    @Override
    public GatewayExecutionResult handleWebhook(WebhookIngestRequest request) {
        Map<String, Object> translatedRequest = orderedMap(
                "signature_header", request.getSignature(),
                "event_type", request.getEventType(),
                "event_id", request.getEventId(),
                "payload", request.getPayload()
        );

        Map<String, Object> translatedResponse = orderedMap(
                "accepted", true,
                "apex_event_ref", generateId("APX-EVT")
        );

        return result(
                "WEBHOOK",
                "/events/inbound",
                request,
                translatedRequest,
                translatedResponse,
                orderedMap(
                        "eventStatus", "PROCESSED",
                        "eventReference", translatedResponse.get("apex_event_ref")
                )
        );
    }
}
