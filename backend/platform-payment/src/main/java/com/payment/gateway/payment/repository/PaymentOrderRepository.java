package com.payment.gateway.payment.repository;

import java.util.List;
import java.util.Optional;

import com.payment.gateway.payment.model.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByMerchantIdAndIdempotencyKey(String merchantId, String idempotencyKey);

    List<PaymentOrder> findAllByOrderByIdDesc();
}
