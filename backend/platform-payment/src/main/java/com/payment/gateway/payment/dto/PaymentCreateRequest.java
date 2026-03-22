package com.payment.gateway.payment.dto;

import java.math.BigDecimal;

import com.payment.gateway.payment.model.PaymentDirection;
import com.payment.gateway.payment.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentCreateRequest {

    @NotBlank
    private String merchantId;
    @NotBlank
    private String customerReference;
    @NotNull
    private PaymentDirection direction;
    @NotNull
    private PaymentMethod paymentMethod;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
    @NotBlank
    private String currency;
    @NotBlank
    private String businessReference;
    @NotBlank
    private String idempotencyKey;
    private String sourceAccountReference;
    private String beneficiaryReference;
    private String requestedChannelCode;
    private String narrative;
    private String purposeCode;
    private String crmCaseId;
    private String salesOwner;
    private String relationshipManager;

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

    public PaymentDirection getDirection() {
        return direction;
    }

    public void setDirection(PaymentDirection direction) {
        this.direction = direction;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
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

    public String getBusinessReference() {
        return businessReference;
    }

    public void setBusinessReference(String businessReference) {
        this.businessReference = businessReference;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
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

    public String getRequestedChannelCode() {
        return requestedChannelCode;
    }

    public void setRequestedChannelCode(String requestedChannelCode) {
        this.requestedChannelCode = requestedChannelCode;
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

    public String getCrmCaseId() {
        return crmCaseId;
    }

    public void setCrmCaseId(String crmCaseId) {
        this.crmCaseId = crmCaseId;
    }

    public String getSalesOwner() {
        return salesOwner;
    }

    public void setSalesOwner(String salesOwner) {
        this.salesOwner = salesOwner;
    }

    public String getRelationshipManager() {
        return relationshipManager;
    }

    public void setRelationshipManager(String relationshipManager) {
        this.relationshipManager = relationshipManager;
    }
}
