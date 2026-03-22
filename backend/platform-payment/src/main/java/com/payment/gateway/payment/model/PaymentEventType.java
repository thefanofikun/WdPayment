package com.payment.gateway.payment.model;

public enum PaymentEventType {
    CREATED,
    CANCELLED,
    MARKED_COMPLETED,
    MARKED_FAILED,
    RETRY_SUBMISSION
}
