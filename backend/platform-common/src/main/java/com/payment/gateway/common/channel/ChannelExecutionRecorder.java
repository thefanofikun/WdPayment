package com.payment.gateway.common.channel;

public interface ChannelExecutionRecorder {

    void recordExecution(String channelCode, String operation, boolean success, long latencyMs, String message);
}
