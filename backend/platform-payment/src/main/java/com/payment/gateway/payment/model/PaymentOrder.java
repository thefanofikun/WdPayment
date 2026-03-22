package com.payment.gateway.payment.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        catalog = "payment_db",
        name = "payment_order",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_order_payment_no", columnNames = "payment_no"),
                @UniqueConstraint(name = "uk_payment_order_merchant_idempotency", columnNames = {"merchant_id", "idempotency_key"})
        },
        indexes = {
                @Index(name = "idx_payment_order_status", columnList = "status"),
                @Index(name = "idx_payment_order_merchant", columnList = "merchant_id")
        }
)
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_no", nullable = false, length = 64)
    private String paymentNo;

    @Column(name = "merchant_id", nullable = false, length = 64)
    private String merchantId;

    @Column(name = "merchant_name", nullable = false, length = 256)
    private String merchantName;

    @Column(name = "customer_reference", nullable = false, length = 64)
    private String customerReference;

    @Column(name = "customer_name", nullable = false, length = 256)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 32)
    private PaymentDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 32)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private PaymentStatus status;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 16)
    private String currency;

    @Column(name = "source_account_reference", length = 64)
    private String sourceAccountReference;

    @Column(name = "beneficiary_reference", length = 64)
    private String beneficiaryReference;

    @Column(name = "beneficiary_name", length = 256)
    private String beneficiaryName;

    @Column(name = "requested_channel_code", length = 32)
    private String requestedChannelCode;

    @Column(name = "routed_channel_code", length = 32)
    private String routedChannelCode;

    @Column(name = "gateway_operation", length = 64)
    private String gatewayOperation;

    @Column(name = "gateway_request_id", length = 64)
    private String gatewayRequestId;

    @Column(name = "gateway_message", length = 512)
    private String gatewayMessage;

    @Column(name = "business_reference", length = 64)
    private String businessReference;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "narrative", length = 512)
    private String narrative;

    @Column(name = "purpose_code", length = 64)
    private String purposeCode;

    @Column(name = "crm_case_id", length = 64)
    private String crmCaseId;

    @Column(name = "sales_owner", length = 128)
    private String salesOwner;

    @Column(name = "relationship_manager", length = 128)
    private String relationshipManager;

    @Column(name = "version_no", nullable = false)
    private int version;

    @Column(name = "created_at", nullable = false, length = 64)
    private String createdAt;

    @Column(name = "updated_at", nullable = false, length = 64)
    private String updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            catalog = "payment_db",
            name = "payment_approval_record",
            joinColumns = @JoinColumn(name = "payment_order_id")
    )
    @OrderColumn(name = "approval_order")
    private List<PaymentApprovalRecord> approvals = new ArrayList<PaymentApprovalRecord>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            catalog = "payment_db",
            name = "payment_event_record",
            joinColumns = @JoinColumn(name = "payment_order_id")
    )
    @OrderColumn(name = "event_order")
    private List<PaymentEventRecord> events = new ArrayList<PaymentEventRecord>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaymentNo() {
        return paymentNo;
    }

    public void setPaymentNo(String paymentNo) {
        this.paymentNo = paymentNo;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
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

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getRequestedChannelCode() {
        return requestedChannelCode;
    }

    public void setRequestedChannelCode(String requestedChannelCode) {
        this.requestedChannelCode = requestedChannelCode;
    }

    public String getRoutedChannelCode() {
        return routedChannelCode;
    }

    public void setRoutedChannelCode(String routedChannelCode) {
        this.routedChannelCode = routedChannelCode;
    }

    public String getGatewayOperation() {
        return gatewayOperation;
    }

    public void setGatewayOperation(String gatewayOperation) {
        this.gatewayOperation = gatewayOperation;
    }

    public String getGatewayRequestId() {
        return gatewayRequestId;
    }

    public void setGatewayRequestId(String gatewayRequestId) {
        this.gatewayRequestId = gatewayRequestId;
    }

    public String getGatewayMessage() {
        return gatewayMessage;
    }

    public void setGatewayMessage(String gatewayMessage) {
        this.gatewayMessage = gatewayMessage;
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<PaymentApprovalRecord> getApprovals() {
        return approvals;
    }

    public List<PaymentEventRecord> getEvents() {
        return events;
    }
}
