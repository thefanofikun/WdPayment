package com.payment.gateway.common.reference.model;

public class MockMerchantProfile {

    private final String merchantId;
    private final String merchantName;
    private final String relationshipManager;
    private final String defaultCurrency;

    public MockMerchantProfile(String merchantId, String merchantName, String relationshipManager, String defaultCurrency) {
        this.merchantId = merchantId;
        this.merchantName = merchantName;
        this.relationshipManager = relationshipManager;
        this.defaultCurrency = defaultCurrency;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getRelationshipManager() {
        return relationshipManager;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }
}
