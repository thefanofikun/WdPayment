package com.payment.gateway.gateway.dto.request;

import java.math.BigDecimal;

import com.payment.gateway.common.model.PayoutType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PayoutRequest {

    @NotBlank
    private String channelCode;
    @NotBlank
    private String merchantId;
    @NotBlank
    private String payoutReference;
    @NotNull
    private PayoutType payoutType;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
    @NotBlank
    private String currency;
    @NotBlank
    private String sourceAccountReference;
    @NotBlank
    private String beneficiaryReference;
    private String narrative;
    private String purposeCode;
    private String valueDate;
    private String sourceAccountNumber;
    private String sourceAccountName;
    private String ultimateSourceAccountNumber;
    private String beneficiaryAccountNumber;
    private String beneficiaryAccountName;
    private String beneficiaryBankCountry;
    private String beneficiaryBankCode;
    private String beneficiarySwiftCode;
    private String beneficiaryBankName;
    private String beneficiaryAddress;
    private String beneficiaryCity;
    private String chargeBearer;
    private String feeCurrency;

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

    public String getPayoutReference() {
        return payoutReference;
    }

    public void setPayoutReference(String payoutReference) {
        this.payoutReference = payoutReference;
    }

    public PayoutType getPayoutType() {
        return payoutType;
    }

    public void setPayoutType(PayoutType payoutType) {
        this.payoutType = payoutType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSourceAccountReference() {
        return sourceAccountReference;
    }

    public void setSourceAccountReference(String sourceAccountReference) {
        this.sourceAccountReference = sourceAccountReference;
    }

    public String getBeneficiaryReference() {
        return beneficiaryReference;
    }

    public void setBeneficiaryReference(String beneficiaryReference) {
        this.beneficiaryReference = beneficiaryReference;
    }

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    public String getPurposeCode() {
        return purposeCode;
    }

    public void setPurposeCode(String purposeCode) {
        this.purposeCode = purposeCode;
    }

    public String getValueDate() {
        return valueDate;
    }

    public void setValueDate(String valueDate) {
        this.valueDate = valueDate;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public String getSourceAccountName() {
        return sourceAccountName;
    }

    public void setSourceAccountName(String sourceAccountName) {
        this.sourceAccountName = sourceAccountName;
    }

    public String getUltimateSourceAccountNumber() {
        return ultimateSourceAccountNumber;
    }

    public void setUltimateSourceAccountNumber(String ultimateSourceAccountNumber) {
        this.ultimateSourceAccountNumber = ultimateSourceAccountNumber;
    }

    public String getBeneficiaryAccountNumber() {
        return beneficiaryAccountNumber;
    }

    public void setBeneficiaryAccountNumber(String beneficiaryAccountNumber) {
        this.beneficiaryAccountNumber = beneficiaryAccountNumber;
    }

    public String getBeneficiaryAccountName() {
        return beneficiaryAccountName;
    }

    public void setBeneficiaryAccountName(String beneficiaryAccountName) {
        this.beneficiaryAccountName = beneficiaryAccountName;
    }

    public String getBeneficiaryBankCountry() {
        return beneficiaryBankCountry;
    }

    public void setBeneficiaryBankCountry(String beneficiaryBankCountry) {
        this.beneficiaryBankCountry = beneficiaryBankCountry;
    }

    public String getBeneficiaryBankCode() {
        return beneficiaryBankCode;
    }

    public void setBeneficiaryBankCode(String beneficiaryBankCode) {
        this.beneficiaryBankCode = beneficiaryBankCode;
    }

    public String getBeneficiarySwiftCode() {
        return beneficiarySwiftCode;
    }

    public void setBeneficiarySwiftCode(String beneficiarySwiftCode) {
        this.beneficiarySwiftCode = beneficiarySwiftCode;
    }

    public String getBeneficiaryBankName() {
        return beneficiaryBankName;
    }

    public void setBeneficiaryBankName(String beneficiaryBankName) {
        this.beneficiaryBankName = beneficiaryBankName;
    }

    public String getBeneficiaryAddress() {
        return beneficiaryAddress;
    }

    public void setBeneficiaryAddress(String beneficiaryAddress) {
        this.beneficiaryAddress = beneficiaryAddress;
    }

    public String getBeneficiaryCity() {
        return beneficiaryCity;
    }

    public void setBeneficiaryCity(String beneficiaryCity) {
        this.beneficiaryCity = beneficiaryCity;
    }

    public String getChargeBearer() {
        return chargeBearer;
    }

    public void setChargeBearer(String chargeBearer) {
        this.chargeBearer = chargeBearer;
    }

    public String getFeeCurrency() {
        return feeCurrency;
    }

    public void setFeeCurrency(String feeCurrency) {
        this.feeCurrency = feeCurrency;
    }
}
