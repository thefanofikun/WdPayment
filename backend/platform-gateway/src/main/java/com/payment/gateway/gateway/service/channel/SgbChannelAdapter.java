package com.payment.gateway.gateway.service.channel;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.common.model.ChannelDescriptor;
import com.payment.gateway.common.model.PayoutType;
import com.payment.gateway.common.reference.PaymentReferenceLookup;
import com.payment.gateway.common.reference.model.MockBeneficiaryProfile;
import com.payment.gateway.common.reference.model.MockSourceAccountProfile;
import com.payment.gateway.gateway.dto.request.BeneficiaryRequest;
import com.payment.gateway.gateway.dto.request.CustomerOnboardingRequest;
import com.payment.gateway.gateway.dto.request.PayoutRequest;
import com.payment.gateway.gateway.dto.request.VirtualAccountRequest;
import com.payment.gateway.gateway.dto.request.WebhookIngestRequest;
import com.payment.gateway.gateway.model.GatewayExecutionResult;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class SgbChannelAdapter extends BaseChannelAdapterSupport {

    private static final String INLINE_ONLY = "INLINE_ONLY";

    private final SgbChannelProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final PaymentReferenceLookup paymentReferenceLookup;

    private PrivateKey cachedPrivateKey;

    public SgbChannelAdapter(SgbChannelProperties properties,
                             ObjectMapper objectMapper,
                             RestTemplateBuilder restTemplateBuilder,
                             PaymentReferenceLookup paymentReferenceLookup) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplateBuilder.build();
        this.paymentReferenceLookup = paymentReferenceLookup;
    }

    @Override
    public String channelCode() {
        return "SGB";
    }

    @Override
    public ChannelDescriptor descriptor() {
        return new ChannelDescriptor(
                channelCode(),
                "Singapore Gulf Bank",
                Arrays.asList("BH", "SG", "AE", "US", "EU"),
                Arrays.asList("VIRTUAL_ACCOUNT", "PAYOUT", "WEBHOOK"),
                "Real SGB mapping is wired for VA, remittance payout, intra-bank payout, and webhook payloads. "
                        + "Current published docs do not expose standalone onboarding or beneficiary registration APIs."
        );
    }

    @Override
    public GatewayExecutionResult onboardCustomer(CustomerOnboardingRequest request) {
        Map<String, Object> translatedRequest = orderedMap(
                "customerReference", request.getCustomerReference(),
                "legalName", request.getLegalName(),
                "country", request.getCountry(),
                "status", "UNSUPPORTED_IN_PUBLISHED_DOCS"
        );

        return result(
                "CUSTOMER_ONBOARDING",
                "N/A",
                request,
                translatedRequest,
                Collections.<String, Object>emptyMap(),
                orderedMap(
                        "supported", false,
                        "reason", "SGB published payment docs do not expose a standalone onboarding endpoint"
                ),
                false,
                "SGB published payment docs do not expose a standalone customer onboarding API"
        );
    }

    @Override
    public GatewayExecutionResult createVirtualAccount(VirtualAccountRequest request) {
        String masterAccountNumber = trimToNull(request.getMasterAccountNumber());
        String masterAccountCurrency = firstNonBlank(request.getMasterAccountCurrency(), request.getCurrency());
        Integer createCount = request.getCreateCount() == null || request.getCreateCount().intValue() <= 0
                ? Integer.valueOf(1)
                : request.getCreateCount();
        String externalRequestId = firstNonBlank(request.getExternalRequestId(), request.getVirtualAccountReference());

        Map<String, Object> translatedRequest = orderedMap(
                "masterAcctNo", masterAccountNumber,
                "masterAcctCcy", masterAccountCurrency,
                "createCount", createCount,
                "externalReqId", externalRequestId
        );

        List<String> missingFields = missingRequired(
                field(masterAccountNumber, "masterAccountNumber"),
                field(masterAccountCurrency, "masterAccountCurrency"),
                field(externalRequestId, "externalRequestId")
        );
        if (!missingFields.isEmpty()) {
            return missingFieldResult(
                    "VIRTUAL_ACCOUNT",
                    "/va/account/create",
                    request,
                    translatedRequest,
                    missingFields
            );
        }

        DownstreamCallResult downstream = invokePost("/va/account/create", translatedRequest, false);
        return result(
                "VIRTUAL_ACCOUNT",
                "/va/account/create",
                request,
                translatedRequest,
                downstream.responseBody,
                buildVirtualAccountResult(downstream.responseBody),
                downstream.success,
                downstream.message
        );
    }

    @Override
    public GatewayExecutionResult createBeneficiary(BeneficiaryRequest request) {
        Map<String, Object> translatedRequest = orderedMap(
                "beneficiaryReference", request.getBeneficiaryReference(),
                "beneficiaryName", request.getBeneficiaryName(),
                "beneficiaryAcctNo", request.getAccountNumber(),
                "beneficiaryBic", firstNonBlank(request.getSwiftCode(), request.getBankCode()),
                "beneficiaryCountry", request.getBankCountry()
        );

        return result(
                "BENEFICIARY",
                INLINE_ONLY,
                request,
                translatedRequest,
                orderedMap(
                        "mode", INLINE_ONLY,
                        "supported", false
                ),
                orderedMap(
                        "beneficiaryReference", request.getBeneficiaryReference(),
                        "registrationMode", INLINE_ONLY
                ),
                true,
                "SGB has no standalone beneficiary API in the published docs. Beneficiary details must be sent inline with payout requests."
        );
    }

    @Override
    public GatewayExecutionResult createPayout(PayoutRequest request) {
        MockSourceAccountProfile sourceAccount = paymentReferenceLookup.findSourceAccount(request.getSourceAccountReference());
        MockBeneficiaryProfile beneficiary = paymentReferenceLookup.findBeneficiary(request.getBeneficiaryReference());

        if (request.getPayoutType() == PayoutType.INTERNAL_TRANSFER) {
            return createIntraBankPayout(request, sourceAccount, beneficiary);
        }
        return createRemittancePayout(request, sourceAccount, beneficiary);
    }

    @Override
    public GatewayExecutionResult handleWebhook(WebhookIngestRequest request) {
        Map<String, Object> payload = request.getPayload() == null
                ? Collections.<String, Object>emptyMap()
                : request.getPayload();
        Map<String, Object> translatedRequest = orderedMap(
                "eventType", firstNonBlank(stringValue(payload.get("eventType")), request.getEventType()),
                "eventName", stringValue(payload.get("eventName")),
                "transactionId", stringValue(payload.get("transactionId")),
                "businessTransactionId", stringValue(payload.get("businessTransactionId")),
                "customerId", stringValue(payload.get("customerId")),
                "payload", payload
        );

        String eventType = stringValue(translatedRequest.get("eventType"));
        Map<String, Object> normalizedResult = orderedMap(
                "eventCategory", firstNonBlank(eventType, request.getEventType()),
                "eventId", firstNonBlank(request.getEventId(), stringValue(payload.get("transactionId"))),
                "transactionId", stringValue(payload.get("transactionId")),
                "businessTransactionId", stringValue(payload.get("businessTransactionId")),
                "status", firstNonBlank(stringValue(payload.get("status")), request.getEventType()),
                "accepted", true
        );

        return result(
                "WEBHOOK",
                "/webhook/sgb/inbound",
                request,
                translatedRequest,
                orderedMap("accepted", true),
                normalizedResult,
                true,
                "SGB webhook payload normalized by gateway"
        );
    }

    private GatewayExecutionResult createIntraBankPayout(PayoutRequest request,
                                                         MockSourceAccountProfile sourceAccount,
                                                         MockBeneficiaryProfile beneficiary) {
        String payerAccountNumber = firstNonBlank(request.getSourceAccountNumber(), accountNumber(sourceAccount));
        String payeeAccountNumber = firstNonBlank(request.getBeneficiaryAccountNumber(), accountNumber(beneficiary));
        String payeeAccountName = firstNonBlank(request.getBeneficiaryAccountName(), beneficiaryName(beneficiary));
        String businessTransactionId = firstNonBlank(request.getPayoutReference(), generateId("SGB-PAYOUT"));

        Map<String, Object> translatedRequest = orderedMap(
                "businessTransactionId", businessTransactionId,
                "payerAcctNo", payerAccountNumber,
                "ultmtPayerAcctNo", firstNonBlank(request.getUltimateSourceAccountNumber(), accountNumber(sourceAccount)),
                "payeeAcctNo", payeeAccountNumber,
                "payeeAcctName", payeeAccountName,
                "tranAmt", request.getAmount(),
                "ccy", request.getCurrency(),
                "remarks", firstNonBlank(request.getNarrative(), request.getPurposeCode())
        );

        List<String> missingFields = missingRequired(
                field(businessTransactionId, "payoutReference"),
                field(payerAccountNumber, "sourceAccountNumber"),
                field(payeeAccountNumber, "beneficiaryAccountNumber"),
                field(payeeAccountName, "beneficiaryAccountName"),
                field(request.getCurrency(), "currency"),
                field(request.getAmount(), "amount")
        );
        if (!missingFields.isEmpty()) {
            return missingFieldResult("PAYOUT", "/payment/intra/transfer", request, translatedRequest, missingFields);
        }

        DownstreamCallResult downstream = invokePost("/payment/intra/transfer", translatedRequest, true);
        return result(
                "PAYOUT",
                "/payment/intra/transfer",
                request,
                translatedRequest,
                downstream.responseBody,
                buildPayoutResult(downstream.responseBody),
                downstream.success,
                downstream.message
        );
    }

    private GatewayExecutionResult createRemittancePayout(PayoutRequest request,
                                                          MockSourceAccountProfile sourceAccount,
                                                          MockBeneficiaryProfile beneficiary) {
        String businessTransactionId = firstNonBlank(request.getPayoutReference(), generateId("SGB-PAYOUT"));
        String senderAccountNumber = firstNonBlank(request.getSourceAccountNumber(), accountNumber(sourceAccount));
        String beneficiaryAccountNumber = firstNonBlank(request.getBeneficiaryAccountNumber(), accountNumber(beneficiary));
        String beneficiaryAccountName = firstNonBlank(request.getBeneficiaryAccountName(), beneficiaryName(beneficiary));
        String beneficiaryCountry = firstNonBlank(request.getBeneficiaryBankCountry(), bankCountry(beneficiary));
        String beneficiaryAddress = firstNonBlank(request.getBeneficiaryAddress(), beneficiaryAddress(beneficiary));
        String beneficiaryCity = firstNonBlank(request.getBeneficiaryCity(), beneficiaryCity(beneficiary));
        String beneficiarySwift = firstNonBlank(
                request.getBeneficiarySwiftCode(),
                request.getBeneficiaryBankCode(),
                swiftCode(beneficiary),
                bankCode(beneficiary)
        );
        String beneficiaryBankName = firstNonBlank(request.getBeneficiaryBankName(), bankName(beneficiary));
        String chargeBearer = firstNonBlank(request.getChargeBearer(), "OUR");
        String feeCurrency = firstNonBlank(request.getFeeCurrency(), request.getCurrency());

        Map<String, Object> translatedRequest = orderedMap(
                "businessTransactionId", businessTransactionId,
                "senderAcctNo", senderAccountNumber,
                "ultmtSenderAcctNo", firstNonBlank(request.getUltimateSourceAccountNumber(), accountNumber(sourceAccount)),
                "beneficiaryCity", beneficiaryCity,
                "beneficiaryAcctNo", beneficiaryAccountNumber,
                "beneficiaryAcctName", beneficiaryAccountName,
                "beneficiaryCountry", beneficiaryCountry,
                "beneficiaryAddress", beneficiaryAddress,
                "beneficiaryBic", beneficiarySwift,
                "beneficiaryBankName", beneficiaryBankName,
                "tranCcy", request.getCurrency(),
                "tranAmt", request.getAmount(),
                "chargeBearer", chargeBearer,
                "feeCcy", feeCurrency,
                "purpose", firstNonBlank(request.getPurposeCode(), "SUPP"),
                "remarks", firstNonBlank(request.getNarrative(), request.getPurposeCode())
        );

        List<String> missingFields = missingRequired(
                field(businessTransactionId, "payoutReference"),
                field(senderAccountNumber, "sourceAccountNumber"),
                field(beneficiaryCity, "beneficiaryCity"),
                field(beneficiaryAccountNumber, "beneficiaryAccountNumber"),
                field(beneficiaryAccountName, "beneficiaryAccountName"),
                field(beneficiaryCountry, "beneficiaryBankCountry"),
                field(beneficiaryAddress, "beneficiaryAddress"),
                field(beneficiarySwift, "beneficiarySwiftCode"),
                field(beneficiaryBankName, "beneficiaryBankName"),
                field(request.getCurrency(), "currency"),
                field(request.getAmount(), "amount")
        );
        if (!missingFields.isEmpty()) {
            return missingFieldResult("PAYOUT", "/payment/remittance/payout", request, translatedRequest, missingFields);
        }

        DownstreamCallResult downstream = invokePost("/payment/remittance/payout", translatedRequest, true);
        return result(
                "PAYOUT",
                "/payment/remittance/payout",
                request,
                translatedRequest,
                downstream.responseBody,
                buildPayoutResult(downstream.responseBody),
                downstream.success,
                downstream.message
        );
    }

    private GatewayExecutionResult missingFieldResult(String operation,
                                                      String endpoint,
                                                      Object normalizedRequest,
                                                      Map<String, Object> translatedRequest,
                                                      List<String> missingFields) {
        return result(
                operation,
                endpoint,
                normalizedRequest,
                translatedRequest,
                Collections.<String, Object>emptyMap(),
                orderedMap("missingFields", missingFields),
                false,
                "SGB mapping is missing required fields: " + missingFields
        );
    }

    private DownstreamCallResult invokePost(String path, Map<String, Object> requestBody, boolean duplicateIsSuccess) {
        if (!properties.isEnabled()) {
            return DownstreamCallResult.failure(
                    "SGB channel is disabled. Set gateway.channel.sgb.enabled=true to enable simulation or real downstream traffic.",
                    Collections.<String, Object>emptyMap()
            );
        }
        if (!isConfigured()) {
            return simulateDownstream(path, requestBody, duplicateIsSuccess);
        }

        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            String signature = sign(buildSignContent(HttpMethod.POST.name(), timestamp, path, jsonBody));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("sgb-client-key", properties.getClientKey());
            headers.add("sgb-request-timestamp", timestamp);
            headers.add("sgb-request-signature", signature);

            ResponseEntity<String> response = restTemplate.exchange(
                    buildUrl(path),
                    HttpMethod.POST,
                    new HttpEntity<String>(jsonBody, headers),
                    String.class
            );

            Map<String, Object> responseBody = parseResponseBody(response.getBody());
            return toDownstreamResult(responseBody, duplicateIsSuccess);
        } catch (HttpStatusCodeException ex) {
            Map<String, Object> responseBody = parseResponseBody(ex.getResponseBodyAsString());
            DownstreamCallResult downstream = toDownstreamResult(responseBody, duplicateIsSuccess);
            if (downstream.message == null || downstream.message.trim().length() == 0) {
                return DownstreamCallResult.failure("SGB HTTP error " + ex.getRawStatusCode(), responseBody);
            }
            return downstream;
        } catch (RestClientException ex) {
            return DownstreamCallResult.failure("SGB HTTP call failed: " + ex.getMessage(), Collections.<String, Object>emptyMap());
        } catch (Exception ex) {
            return DownstreamCallResult.failure("SGB signing or serialization failed: " + ex.getMessage(), Collections.<String, Object>emptyMap());
        }
    }

    private DownstreamCallResult simulateDownstream(String path, Map<String, Object> requestBody, boolean duplicateIsSuccess) {
        Map<String, Object> responseBody;
        if ("/va/account/create".equals(path)) {
            responseBody = orderedMap(
                    "code", 0,
                    "msg", "SIMULATED_SUCCESS",
                    "data", orderedMap(
                            "externalReqId", requestBody.get("externalReqId"),
                            "created", requestBody.get("createCount"),
                            "virtualAcctList", Collections.singletonList(orderedMap(
                                    "virtualAcctNo", "SGBVA" + System.currentTimeMillis(),
                                    "masterAcctNo", requestBody.get("masterAcctNo"),
                                    "acctCcy", requestBody.get("masterAcctCcy"),
                                    "status", "ACTIVE"
                            )),
                            "simulation", true
                    )
            );
        } else if ("/payment/intra/transfer".equals(path) || "/payment/remittance/payout".equals(path)) {
            String businessTransactionId = firstNonBlank(
                    stringValue(requestBody.get("businessTransactionId")),
                    stringValue(requestBody.get("externalReqId")),
                    generateId("SGB-PAYOUT")
            );
            responseBody = orderedMap(
                    "code", 0,
                    "msg", "SIMULATED_SUCCESS",
                    "data", orderedMap(
                            "businessTransactionId", businessTransactionId,
                            "transactionId", generateId("SGBTX"),
                            "reference", generateId("SGBREF"),
                            "status", duplicateIsSuccess ? "ACCEPTED_OR_DUPLICATE" : "ACCEPTED",
                            "simulation", true
                    )
            );
        } else {
            responseBody = orderedMap(
                    "code", 0,
                    "msg", "SIMULATED_SUCCESS",
                    "data", orderedMap(
                            "path", path,
                            "simulation", true
                    )
            );
        }
        return new DownstreamCallResult(true, "SGB simulation mode: credentials are not configured yet", responseBody);
    }

    private DownstreamCallResult toDownstreamResult(Map<String, Object> responseBody, boolean duplicateIsSuccess) {
        Integer code = integerValue(responseBody.get("code"));
        boolean success = code != null && code.intValue() == 0;
        if (!success && duplicateIsSuccess && code != null && code.intValue() == 100008) {
            success = true;
        }

        String message = firstNonBlank(stringValue(responseBody.get("msg")), success ? "SUCCESS" : "FAILED");
        return new DownstreamCallResult(success, message, responseBody);
    }

    private Map<String, Object> buildVirtualAccountResult(Map<String, Object> responseBody) {
        Map<String, Object> data = nestedData(responseBody);
        return orderedMap(
                "externalRequestId", stringValue(data.get("externalReqId")),
                "created", data.get("created"),
                "virtualAccountList", data.get("virtualAcctList")
        );
    }

    private Map<String, Object> buildPayoutResult(Map<String, Object> responseBody) {
        Map<String, Object> data = nestedData(responseBody);
        return orderedMap(
                "businessTransactionId", firstNonBlank(stringValue(data.get("businessTransactionId")), stringValue(data.get("externalReqId"))),
                "transactionId", stringValue(data.get("transactionId")),
                "reference", stringValue(data.get("reference")),
                "status", stringValue(data.get("status")),
                "errorReason", stringValue(data.get("errorReason"))
        );
    }

    private Map<String, Object> parseResponseBody(String body) {
        if (body == null || body.trim().length() == 0) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ignored) {
            return orderedMap("rawBody", body);
        }
    }

    private synchronized String sign(String content) throws GeneralSecurityException {
        PrivateKey privateKey = loadPrivateKey();
        Signature signature = Signature.getInstance("SHA384withECDSA");
        signature.initSign(privateKey);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signature.sign());
    }

    private synchronized PrivateKey loadPrivateKey() throws GeneralSecurityException {
        if (cachedPrivateKey != null) {
            return cachedPrivateKey;
        }
        String pem = trimToNull(properties.getPrivateKeyPem());
        if (pem == null) {
            throw new GeneralSecurityException("SGB private key is empty");
        }

        String normalized = pem
                .replace("\\n", "")
                .replace("\\r", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN EC PRIVATE KEY-----", "")
                .replace("-----END EC PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(normalized);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        cachedPrivateKey = KeyFactory.getInstance("EC").generatePrivate(keySpec);
        return cachedPrivateKey;
    }

    private String buildSignContent(String method, String timestamp, String path, String jsonBody) {
        StringBuilder builder = new StringBuilder();
        builder.append(method).append("&").append(timestamp).append("&").append(path);
        if (jsonBody != null && jsonBody.trim().length() > 0) {
            builder.append("&").append(jsonBody);
        }
        return builder.toString();
    }

    private boolean isConfigured() {
        return properties.isEnabled()
                && trimToNull(properties.getApiBaseUrl()) != null
                && trimToNull(properties.getClientKey()) != null
                && trimToNull(properties.getPrivateKeyPem()) != null;
    }

    private String buildUrl(String path) {
        String baseUrl = trimToNull(properties.getApiBaseUrl());
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + path;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> nestedData(Map<String, Object> responseBody) {
        Object data = responseBody.get("data");
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        return Collections.emptyMap();
    }

    private Integer integerValue(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return Integer.valueOf(((Number) value).intValue());
        }
        if (value instanceof String && ((String) value).trim().length() > 0) {
            try {
                return Integer.valueOf((String) value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String accountNumber(MockSourceAccountProfile sourceAccount) {
        return sourceAccount == null ? null : trimToNull(sourceAccount.getAccountNumber());
    }

    private String accountNumber(MockBeneficiaryProfile beneficiary) {
        return beneficiary == null ? null : trimToNull(beneficiary.getAccountNumber());
    }

    private String beneficiaryName(MockBeneficiaryProfile beneficiary) {
        return beneficiary == null ? null : trimToNull(beneficiary.getBeneficiaryName());
    }

    private String bankCountry(MockBeneficiaryProfile beneficiary) {
        return beneficiary == null ? null : trimToNull(beneficiary.getBankCountry());
    }

    private String bankCode(MockBeneficiaryProfile beneficiary) {
        return beneficiary == null ? null : trimToNull(beneficiary.getBankCode());
    }

    private String swiftCode(MockBeneficiaryProfile beneficiary) {
        return beneficiary == null ? null : trimToNull(beneficiary.getSwiftCode());
    }

    private String bankName(MockBeneficiaryProfile beneficiary) {
        return beneficiary == null ? null : trimToNull(beneficiary.getBankName());
    }

    private String beneficiaryAddress(MockBeneficiaryProfile beneficiary) {
        return beneficiary == null ? null : trimToNull(beneficiary.getAddress());
    }

    private String beneficiaryCity(MockBeneficiaryProfile beneficiary) {
        return beneficiary == null ? null : trimToNull(beneficiary.getCity());
    }

    private List<String> missingRequired(FieldCheck... checks) {
        List<String> missing = new ArrayList<String>();
        for (FieldCheck check : checks) {
            if (!check.present) {
                missing.add(check.fieldName);
            }
        }
        return missing;
    }

    private FieldCheck field(Object value, String fieldName) {
        if (value == null) {
            return new FieldCheck(fieldName, false);
        }
        if (value instanceof String) {
            return new FieldCheck(fieldName, trimToNull((String) value) != null);
        }
        return new FieldCheck(fieldName, true);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            String trimmed = trimToNull(value);
            if (trimmed != null) {
                return trimmed;
            }
        }
        return null;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() == 0 ? null : trimmed;
    }

    private static final class DownstreamCallResult {
        private final boolean success;
        private final String message;
        private final Map<String, Object> responseBody;

        private DownstreamCallResult(boolean success, String message, Map<String, Object> responseBody) {
            this.success = success;
            this.message = message;
            this.responseBody = responseBody;
        }

        private static DownstreamCallResult failure(String message, Map<String, Object> responseBody) {
            return new DownstreamCallResult(false, message, responseBody);
        }
    }

    private static final class FieldCheck {
        private final String fieldName;
        private final boolean present;

        private FieldCheck(String fieldName, boolean present) {
            this.fieldName = fieldName;
            this.present = present;
        }
    }
}
