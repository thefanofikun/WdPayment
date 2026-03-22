package com.payment.gateway.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class PaymentApprovalRecord {

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false, length = 32)
    private ApprovalStage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 32)
    private ApprovalDecision decision;

    @Column(name = "actor", nullable = false, length = 128)
    private String actor;

    @Column(name = "comment_text", length = 512)
    private String comment;

    @Column(name = "acted_at", nullable = false, length = 64)
    private String actedAt;

    protected PaymentApprovalRecord() {
    }

    public PaymentApprovalRecord(ApprovalStage stage, ApprovalDecision decision, String actor,
                                 String comment, String actedAt) {
        this.stage = stage;
        this.decision = decision;
        this.actor = actor;
        this.comment = comment;
        this.actedAt = actedAt;
    }

    public ApprovalStage getStage() {
        return stage;
    }

    public ApprovalDecision getDecision() {
        return decision;
    }

    public String getActor() {
        return actor;
    }

    public String getComment() {
        return comment;
    }

    public String getActedAt() {
        return actedAt;
    }
}
