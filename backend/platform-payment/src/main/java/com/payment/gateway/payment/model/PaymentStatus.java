package com.payment.gateway.payment.model;

public enum PaymentStatus {
    PENDING_CHECKER_REVIEW,
    PENDING_L1_REVIEW,
    PENDING_L2_REVIEW,
    GATEWAY_SUBMITTED,
    PROCESSING,
    COMPLETED,
    CANCELLED,
    REJECTED,
    FAILED
}
