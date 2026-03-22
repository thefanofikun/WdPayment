package com.payment.gateway.ops.repository;

import java.util.List;
import java.util.Optional;

import com.payment.gateway.ops.entity.ChannelMetricSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelMetricSnapshotRepository extends JpaRepository<ChannelMetricSnapshotEntity, Long> {

    Optional<ChannelMetricSnapshotEntity> findByChannelCodeAndOperationCode(String channelCode, String operationCode);

    List<ChannelMetricSnapshotEntity> findAllByOrderByOperationCodeAscChannelCodeAsc();
}
