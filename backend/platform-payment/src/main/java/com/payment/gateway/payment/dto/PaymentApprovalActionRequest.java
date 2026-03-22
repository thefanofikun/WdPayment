package com.payment.gateway.payment.dto;

import jakarta.validation.constraints.NotBlank;

public class PaymentApprovalActionRequest {

    @NotBlank
    private String actor;
    private String comment;

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
