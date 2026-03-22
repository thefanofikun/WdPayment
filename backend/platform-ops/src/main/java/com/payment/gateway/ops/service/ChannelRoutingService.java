package com.payment.gateway.ops.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.payment.gateway.common.channel.ChannelCatalogProvider;
import com.payment.gateway.common.model.ChannelDescriptor;
import com.payment.gateway.ops.model.ChannelMetricSnapshot;
import com.payment.gateway.ops.model.ChannelRouteHistoryRecord;
import com.payment.gateway.ops.model.ChannelRouteRecommendation;
import com.payment.gateway.ops.entity.ChannelRouteHistoryEntity;
import com.payment.gateway.ops.repository.ChannelRouteHistoryRepository;
import org.springframework.stereotype.Service;

@Service
public class ChannelRoutingService {

    private final ChannelCatalogProvider channelCatalogProvider;
    private final ChannelMonitoringService channelMonitoringService;
    private final ChannelRouteHistoryRepository channelRouteHistoryRepository;

    public ChannelRoutingService(ChannelCatalogProvider channelCatalogProvider,
                                 ChannelMonitoringService channelMonitoringService,
                                 ChannelRouteHistoryRepository channelRouteHistoryRepository) {
        this.channelCatalogProvider = channelCatalogProvider;
        this.channelMonitoringService = channelMonitoringService;
        this.channelRouteHistoryRepository = channelRouteHistoryRepository;
    }

    public List<ChannelRouteRecommendation> recommendChannels(String operation) {
        List<ChannelRouteRecommendation> recommendations = new ArrayList<ChannelRouteRecommendation>();
        List<Candidate> candidates = new ArrayList<Candidate>();

        for (ChannelDescriptor descriptor : channelCatalogProvider.getSupportedChannels()) {
            if (descriptor.getCapabilities().contains(operation)) {
                ChannelMetricSnapshot snapshot = channelMonitoringService.getSnapshot(descriptor.getCode(), operation);
                candidates.add(new Candidate(descriptor, snapshot));
            }
        }

        Collections.sort(candidates, new Comparator<Candidate>() {
            @Override
            public int compare(Candidate left, Candidate right) {
                int successCompare = Double.compare(right.snapshot.getSuccessRate(), left.snapshot.getSuccessRate());
                if (successCompare != 0) {
                    return successCompare;
                }
                int latencyCompare = Long.compare(left.snapshot.getAverageLatencyMs(), right.snapshot.getAverageLatencyMs());
                if (latencyCompare != 0) {
                    return latencyCompare;
                }
                return left.descriptor.getCode().compareTo(right.descriptor.getCode());
            }
        });

        int rank = 1;
        for (Candidate candidate : candidates) {
            recommendations.add(new ChannelRouteRecommendation(
                    rank++,
                    operation,
                    candidate.descriptor.getCode(),
                    candidate.descriptor.getName(),
                    candidate.snapshot.getSuccessRate(),
                    candidate.snapshot.getAverageLatencyMs(),
                    candidate.snapshot.getTotalCount(),
                    channelMonitoringService.buildRecommendationReason(candidate.snapshot)
            ));
        }

        persistRouteHistory(operation, recommendations);

        return recommendations;
    }

    public String pickBestChannel(String operation) {
        List<ChannelRouteRecommendation> recommendations = recommendChannels(operation);
        if (recommendations.isEmpty()) {
            return null;
        }
        return recommendations.get(0).getChannelCode();
    }

    public List<ChannelRouteHistoryRecord> getRecentRouteHistory(String operation) {
        List<ChannelRouteHistoryEntity> entities = operation == null || operation.trim().isEmpty()
                ? channelRouteHistoryRepository.findTop20ByOrderByIdDesc()
                : channelRouteHistoryRepository.findTop20ByOperationCodeOrderByIdDesc(operation);
        List<ChannelRouteHistoryRecord> records = new ArrayList<ChannelRouteHistoryRecord>();
        for (ChannelRouteHistoryEntity entity : entities) {
            records.add(new ChannelRouteHistoryRecord(
                    entity.getId(),
                    entity.getOperationCode(),
                    entity.getChannelCode(),
                    resolveChannelName(entity.getChannelCode()),
                    entity.getRouteRank(),
                    entity.getScoreReason(),
                    entity.getSuccessRate() == null ? 0D : entity.getSuccessRate().doubleValue(),
                    entity.getAverageLatencyMs() == null ? 0L : entity.getAverageLatencyMs().longValue(),
                    entity.getTotalCount() == null ? 0L : entity.getTotalCount().longValue(),
                    entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString()
            ));
        }
        return records;
    }

    private void persistRouteHistory(String operation, List<ChannelRouteRecommendation> recommendations) {
        for (ChannelRouteRecommendation recommendation : recommendations) {
            ChannelRouteHistoryEntity entity = new ChannelRouteHistoryEntity();
            entity.setOperationCode(operation);
            entity.setChannelCode(recommendation.getChannelCode());
            entity.setRouteRank(recommendation.getRank());
            entity.setScoreReason(recommendation.getRecommendationReason());
            entity.setSuccessRate(BigDecimal.valueOf(recommendation.getSuccessRate()));
            entity.setAverageLatencyMs(Long.valueOf(recommendation.getAverageLatencyMs()));
            entity.setTotalCount(Long.valueOf(recommendation.getTotalCount()));
            channelRouteHistoryRepository.save(entity);
        }
    }

    private String resolveChannelName(String channelCode) {
        for (ChannelDescriptor descriptor : channelCatalogProvider.getSupportedChannels()) {
            if (descriptor.getCode().equals(channelCode)) {
                return descriptor.getName();
            }
        }
        return channelCode;
    }

    private static class Candidate {
        private final ChannelDescriptor descriptor;
        private final ChannelMetricSnapshot snapshot;

        private Candidate(ChannelDescriptor descriptor, ChannelMetricSnapshot snapshot) {
            this.descriptor = descriptor;
            this.snapshot = snapshot;
        }
    }
}
