package com.payment.gateway.ops.repository;

import java.util.List;

import com.payment.gateway.ops.entity.ChannelRouteHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRouteHistoryRepository extends JpaRepository<ChannelRouteHistoryEntity, Long> {

    List<ChannelRouteHistoryEntity> findTop20ByOrderByIdDesc();

    List<ChannelRouteHistoryEntity> findTop20ByOperationCodeOrderByIdDesc(String operationCode);
}
