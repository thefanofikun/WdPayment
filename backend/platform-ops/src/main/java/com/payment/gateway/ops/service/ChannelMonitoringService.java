package com.payment.gateway.ops.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.payment.gateway.common.channel.ChannelCatalogProvider;
import com.payment.gateway.common.channel.ChannelExecutionRecorder;
import com.payment.gateway.common.model.ChannelDescriptor;
import com.payment.gateway.ops.model.ChannelMetricSnapshot;
import com.payment.gateway.ops.entity.ChannelMetricSnapshotEntity;
import com.payment.gateway.ops.repository.ChannelMetricSnapshotRepository;
import org.springframework.stereotype.Service;

@Service
public class ChannelMonitoringService implements ChannelExecutionRecorder {

    private static final String[] OPERATIONS = {
            "CUSTOMER_ONBOARDING", "VIRTUAL_ACCOUNT", "BENEFICIARY", "PAYOUT", "WEBHOOK"
    };

    private final Map<String, ChannelMetricAccumulator> metrics = new ConcurrentHashMap<String, ChannelMetricAccumulator>();
    private final DecimalFormat rateFormat = new DecimalFormat("0.00");
    private final ChannelMetricSnapshotRepository channelMetricSnapshotRepository;

    public ChannelMonitoringService(ChannelCatalogProvider channelCatalogProvider,
                                    ChannelMetricSnapshotRepository channelMetricSnapshotRepository) {
        this.channelMetricSnapshotRepository = channelMetricSnapshotRepository;
        seedHistoricalMetrics(channelCatalogProvider.getSupportedChannels());
    }

    @Override
    public void recordExecution(String channelCode, String operation, boolean success, long latencyMs, String message) {
        ChannelMetricAccumulator accumulator = getOrCreate(channelCode, operation);
        accumulator.record(success, latencyMs, message);
        saveSnapshot(accumulator.toSnapshot());
    }

    public List<ChannelMetricSnapshot> getSnapshots() {
        List<ChannelMetricSnapshot> snapshots = new ArrayList<ChannelMetricSnapshot>();
        for (ChannelMetricAccumulator accumulator : metrics.values()) {
            snapshots.add(accumulator.toSnapshot());
        }
        Collections.sort(snapshots, new java.util.Comparator<ChannelMetricSnapshot>() {
            @Override
            public int compare(ChannelMetricSnapshot left, ChannelMetricSnapshot right) {
                int operationCompare = left.getOperation().compareTo(right.getOperation());
                if (operationCompare != 0) {
                    return operationCompare;
                }
                return left.getChannelCode().compareTo(right.getChannelCode());
            }
        });
        return snapshots;
    }

    public ChannelMetricSnapshot getSnapshot(String channelCode, String operation) {
        return getOrCreate(channelCode, operation).toSnapshot();
    }

    public String buildRecommendationReason(ChannelMetricSnapshot snapshot) {
        return "successRate=" + rateFormat.format(snapshot.getSuccessRate())
                + "%, avgLatencyMs=" + snapshot.getAverageLatencyMs()
                + ", totalCount=" + snapshot.getTotalCount();
    }

    private ChannelMetricAccumulator getOrCreate(String channelCode, String operation) {
        String key = channelCode + "#" + operation;
        ChannelMetricAccumulator existing = metrics.get(key);
        if (existing != null) {
            return existing;
        }
        ChannelMetricAccumulator created = new ChannelMetricAccumulator(channelCode, operation);
        ChannelMetricAccumulator previous = metrics.putIfAbsent(key, created);
        return previous != null ? previous : created;
    }

    private void seedHistoricalMetrics(List<ChannelDescriptor> descriptors) {
        Map<String, ChannelMetricSnapshotEntity> persisted = new HashMap<String, ChannelMetricSnapshotEntity>();
        for (ChannelMetricSnapshotEntity entity : channelMetricSnapshotRepository.findAll()) {
            persisted.put(entity.getChannelCode() + "#" + entity.getOperationCode(), entity);
        }
        for (ChannelDescriptor descriptor : descriptors) {
            for (String operation : OPERATIONS) {
                String key = descriptor.getCode() + "#" + operation;
                ChannelMetricSnapshotEntity entity = persisted.get(key);
                if (entity != null) {
                    metrics.put(key, ChannelMetricAccumulator.fromSnapshot(toSnapshot(entity)));
                } else {
                    ChannelMetricAccumulator accumulator = getOrCreate(descriptor.getCode(), operation);
                    if ("APEX_PAY".equals(descriptor.getCode())) {
                        accumulator.seed(500, historicalSuccess(500, operation, 0.975), historicalLatency(operation, 290));
                    } else if ("HARBOR_SWITCH".equals(descriptor.getCode())) {
                        accumulator.seed(500, historicalSuccess(500, operation, 0.942), historicalLatency(operation, 360));
                    } else if ("SGB".equals(descriptor.getCode())) {
                        accumulator.seed(320, historicalSuccess(320, operation, 0.958), historicalLatency(operation, 330));
                    } else {
                        accumulator.seed(200, historicalSuccess(200, operation, 0.930), historicalLatency(operation, 410));
                    }
                    saveSnapshot(accumulator.toSnapshot());
                }
            }
        }
    }

    private ChannelMetricSnapshot toSnapshot(ChannelMetricSnapshotEntity entity) {
        return new ChannelMetricSnapshot(
                entity.getChannelCode(),
                entity.getOperationCode(),
                entity.getTotalCount(),
                entity.getSuccessCount(),
                entity.getFailureCount(),
                entity.getSuccessRate() == null ? 0D : entity.getSuccessRate().doubleValue(),
                entity.getAverageLatencyMs(),
                entity.getLastStatus(),
                entity.getLastMessage(),
                entity.getLastUpdatedAt()
        );
    }

    private void saveSnapshot(ChannelMetricSnapshot snapshot) {
        ChannelMetricSnapshotEntity entity = channelMetricSnapshotRepository
                .findByChannelCodeAndOperationCode(snapshot.getChannelCode(), snapshot.getOperation())
                .orElseGet(ChannelMetricSnapshotEntity::new);
        entity.setChannelCode(snapshot.getChannelCode());
        entity.setOperationCode(snapshot.getOperation());
        entity.setTotalCount(snapshot.getTotalCount());
        entity.setSuccessCount(snapshot.getSuccessCount());
        entity.setFailureCount(snapshot.getFailureCount());
        entity.setSuccessRate(BigDecimal.valueOf(snapshot.getSuccessRate()));
        entity.setAverageLatencyMs(snapshot.getAverageLatencyMs());
        entity.setLastStatus(snapshot.getLastStatus());
        entity.setLastMessage(snapshot.getLastMessage());
        entity.setLastUpdatedAt(snapshot.getLastUpdatedAt());
        channelMetricSnapshotRepository.save(entity);
    }

    private long historicalSuccess(long totalCount, String operation, double baseRate) {
        if ("PAYOUT".equals(operation)) {
            return Math.round(totalCount * (baseRate - 0.015));
        }
        if ("WEBHOOK".equals(operation)) {
            return Math.round(totalCount * (baseRate + 0.01));
        }
        return Math.round(totalCount * baseRate);
    }

    private long historicalLatency(String operation, long baseLatency) {
        if ("PAYOUT".equals(operation)) {
            return baseLatency + 70;
        }
        if ("VIRTUAL_ACCOUNT".equals(operation)) {
            return baseLatency - 35;
        }
        return baseLatency;
    }

    private static class ChannelMetricAccumulator {

        private final String channelCode;
        private final String operation;
        private long totalCount;
        private long successCount;
        private long failureCount;
        private long totalLatencyMs;
        private String lastStatus = "UNKNOWN";
        private String lastMessage = "No traffic yet";
        private Instant lastUpdatedAt = Instant.now();

        private ChannelMetricAccumulator(String channelCode, String operation) {
            this.channelCode = channelCode;
            this.operation = operation;
        }

        private synchronized void seed(long totalCount, long successCount, long averageLatencyMs) {
            if (this.totalCount > 0) {
                return;
            }
            this.totalCount = totalCount;
            this.successCount = successCount;
            this.failureCount = totalCount - successCount;
            this.totalLatencyMs = totalCount * averageLatencyMs;
            this.lastStatus = "SEEDED";
            this.lastMessage = "Historical baseline loaded";
            this.lastUpdatedAt = Instant.now();
        }

        private synchronized void record(boolean success, long latencyMs, String message) {
            totalCount += 1;
            if (success) {
                successCount += 1;
                lastStatus = "SUCCESS";
            } else {
                failureCount += 1;
                lastStatus = "FAILURE";
            }
            totalLatencyMs += latencyMs;
            lastMessage = message;
            lastUpdatedAt = Instant.now();
        }

        private synchronized ChannelMetricSnapshot toSnapshot() {
            long averageLatencyMs = totalCount == 0 ? 0 : totalLatencyMs / totalCount;
            double successRate = totalCount == 0 ? 0D : (successCount * 100.0D) / totalCount;
            return new ChannelMetricSnapshot(
                    channelCode,
                    operation,
                    totalCount,
                    successCount,
                    failureCount,
                    successRate,
                    averageLatencyMs,
                    lastStatus,
                    lastMessage,
                    lastUpdatedAt.toString()
            );
        }

        private static ChannelMetricAccumulator fromSnapshot(ChannelMetricSnapshot snapshot) {
            ChannelMetricAccumulator accumulator = new ChannelMetricAccumulator(snapshot.getChannelCode(), snapshot.getOperation());
            accumulator.totalCount = snapshot.getTotalCount();
            accumulator.successCount = snapshot.getSuccessCount();
            accumulator.failureCount = snapshot.getFailureCount();
            accumulator.totalLatencyMs = snapshot.getAverageLatencyMs() * Math.max(snapshot.getTotalCount(), 1L);
            accumulator.lastStatus = snapshot.getLastStatus();
            accumulator.lastMessage = snapshot.getLastMessage();
            accumulator.lastUpdatedAt = snapshot.getLastUpdatedAt() == null
                    ? Instant.now()
                    : Instant.parse(snapshot.getLastUpdatedAt());
            return accumulator;
        }
    }
}
