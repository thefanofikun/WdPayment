package com.payment.gateway.common.reference.model;

public class MockCustomerProfile {

    private final String customerReference;
    private final String customerName;
    private final String merchantId;

    public MockCustomerProfile(String customerReference, String customerName, String merchantId) {
        this.customerReference = customerReference;
        this.customerName = customerName;
        this.merchantId = merchantId;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getMerchantId() {
        return merchantId;
    }
}
