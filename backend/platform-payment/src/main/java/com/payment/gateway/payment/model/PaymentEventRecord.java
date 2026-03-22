package com.payment.gateway.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class PaymentEventRecord {

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64)
    private PaymentEventType eventType;

    @Column(name = "actor", nullable = false, length = 128)
    private String actor;

    @Column(name = "comment_text", length = 512)
    private String comment;

    @Column(name = "event_at", nullable = false, length = 64)
    private String eventAt;

    protected PaymentEventRecord() {
    }

    public PaymentEventRecord(PaymentEventType eventType, String actor, String comment, String eventAt) {
        this.eventType = eventType;
        this.actor = actor;
        this.comment = comment;
        this.eventAt = eventAt;
    }

    public PaymentEventType getEventType() {
        return eventType;
    }

    public String getActor() {
        return actor;
    }

    public String getComment() {
        return comment;
    }

    public String getEventAt() {
        return eventAt;
    }
}
