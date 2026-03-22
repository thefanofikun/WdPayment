package com.payment.gateway.common.api;

import java.util.Map;

public class ErrorResponse {

    private final boolean success;
    private final String message;
    private final Map<String, String> fieldErrors;

    public ErrorResponse(boolean success, String message, Map<String, String> fieldErrors) {
        this.success = success;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public static ErrorResponse of(String message, Map<String, String> fieldErrors) {
        return new ErrorResponse(false, message, fieldErrors);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
