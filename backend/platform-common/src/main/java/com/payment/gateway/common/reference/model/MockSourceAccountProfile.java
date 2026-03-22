package com.payment.gateway.common.reference.model;

public class MockSourceAccountProfile {

    private final String sourceAccountReference;
    private final String merchantId;
    private final String currency;
    private final String accountName;
    private final String accountNumber;

    public MockSourceAccountProfile(String sourceAccountReference, String merchantId,
                                    String currency, String accountName, String accountNumber) {
        this.sourceAccountReference = sourceAccountReference;
        this.merchantId = merchantId;
        this.currency = currency;
        this.accountName = accountName;
        this.accountNumber = accountNumber;
    }

    public String getSourceAccountReference() {
        return sourceAccountReference;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
