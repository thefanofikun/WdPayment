package com.payment.gateway.common.exception;

public class UnsupportedChannelException extends RuntimeException {

    public UnsupportedChannelException(String channelCode) {
        super("Unsupported channel: " + channelCode);
    }
}
