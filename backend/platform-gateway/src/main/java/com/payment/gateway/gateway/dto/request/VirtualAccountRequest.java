package com.payment.gateway.gateway.dto.request;

import jakarta.validation.constraints.NotBlank;

public class VirtualAccountRequest {

    @NotBlank
    private String channelCode;
    @NotBlank
    private String merchantId;
    @NotBlank
    private String customerReference;
    @NotBlank
    private String virtualAccountReference;
    @NotBlank
    private String accountName;
    @NotBlank
    private String currency;
    @NotBlank
    private String country;
    @NotBlank
    private String bankCode;
    private String purpose;
    private String masterAccountNumber;
    private String masterAccountCurrency;
    private Integer createCount;
    private String externalRequestId;

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }

    public String getVirtualAccountReference() {
        return virtualAccountReference;
    }

    public void setVirtualAccountReference(String virtualAccountReference) {
        this.virtualAccountReference = virtualAccountReference;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getMasterAccountNumber() {
        return masterAccountNumber;
    }

    public void setMasterAccountNumber(String masterAccountNumber) {
        this.masterAccountNumber = masterAccountNumber;
    }

    public String getMasterAccountCurrency() {
        return masterAccountCurrency;
    }

    public void setMasterAccountCurrency(String masterAccountCurrency) {
        this.masterAccountCurrency = masterAccountCurrency;
    }

    public Integer getCreateCount() {
        return createCount;
    }

    public void setCreateCount(Integer createCount) {
        this.createCount = createCount;
    }

    public String getExternalRequestId() {
        return externalRequestId;
    }

    public void setExternalRequestId(String externalRequestId) {
        this.externalRequestId = externalRequestId;
    }
}
