package com.payment.gateway.payment.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import com.payment.gateway.common.api.ApiResponse;
import com.payment.gateway.payment.dto.PaymentApprovalActionRequest;
import com.payment.gateway.payment.dto.PaymentCreateRequest;
import com.payment.gateway.payment.model.PaymentDirection;
import com.payment.gateway.payment.model.PaymentStatus;
import com.payment.gateway.payment.service.PaymentReferenceDataService;
import com.payment.gateway.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentReferenceDataService paymentReferenceDataService;

    public PaymentController(PaymentService paymentService, PaymentReferenceDataService paymentReferenceDataService) {
        this.paymentService = paymentService;
        this.paymentReferenceDataService = paymentReferenceDataService;
    }

    @GetMapping("/reference-data")
    public ApiResponse<Object> getReferenceData() {
        return ApiResponse.success(paymentReferenceDataService.getReferenceData());
    }

    @GetMapping("/orders")
    public ApiResponse<Map<String, Object>> listOrders(
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) PaymentDirection direction,
            @RequestParam(required = false) String channelCode,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("orders", paymentService.listPayments(merchantId, status, direction, channelCode, keyword));
        return ApiResponse.success(response);
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<Object> getOrder(@PathVariable Long id) {
        return ApiResponse.success(paymentService.getPayment(id));
    }

    @PostMapping("/orders")
    public ApiResponse<Object> createOrder(@Valid @RequestBody PaymentCreateRequest request) {
        return ApiResponse.success(paymentService.createPayment(request));
    }

    @PostMapping("/orders/{id}/checker/approve")
    public ApiResponse<Object> checkerApprove(@PathVariable Long id,
                                              @Valid @RequestBody PaymentApprovalActionRequest request) {
        return ApiResponse.success(paymentService.checkerApprove(id, request));
    }

    @PostMapping("/orders/{id}/checker/reject")
    public ApiResponse<Object> checkerReject(@PathVariable Long id,
                                             @Valid @RequestBody PaymentApprovalActionRequest request) {
        return ApiResponse.success(paymentService.checkerReject(id, request));
    }

    @PostMapping("/orders/{id}/l1/approve")
    public ApiResponse<Object> l1Approve(@PathVariable Long id,
                                         @Valid @RequestBody PaymentApprovalActionRequest request) {
        return ApiResponse.success(paymentService.l1Approve(id, request));
    }

    @PostMapping("/orders/{id}/l1/reject")
    public ApiResponse<Object> l1Reject(@PathVariable Long id,
                                        @Valid @RequestBody PaymentApprovalActionRequest request) {
        return ApiResponse.success(paymentService.l1Reject(id, request));
    }

    @PostMapping("/orders/{id}/l2/approve")
    public ApiResponse<Object> l2Approve(@PathVariable Long id,
                                         @Valid @RequestBody PaymentApprovalActionRequest request) {
        return ApiResponse.success(paymentService.l2Approve(id, request));
    }

    @PostMapping("/orders/{id}/l2/reject")
    public ApiResponse<Object> l2Reject(@PathVariable Long id,
                                        @Valid @RequestBody PaymentApprovalActionRequest request) {
        return ApiResponse.success(paymentService.l2Reject(id, request));
    }

    @PostMapping("/orders/{id}/cancel")
    public ApiResponse<Object> cancelOrder(@PathVariable Long id,
                                           @Valid @RequestBody PaymentApprovalActionRequest request) {
        return ApiResponse.success(paymentService.cancelPayment(id, request));
    }

    @PostMapping("/orders/{id}/ops/complete")
    public ApiResponse<Object> markCompleted(@PathVariable Long id,
                                             @Valid @RequestBody PaymentApprovalActionRequest request) {
        return ApiResponse.success(paymentService.markCompleted(id, request));
    }

    @PostMapping("/orders/{id}/ops/fail")
    public ApiResponse<Object> markFailed(@PathVariable Long id,
                                          @Valid @RequestBody PaymentApprovalActionRequest request) {
        return ApiResponse.success(paymentService.markFailed(id, request));
    }

    @PostMapping("/orders/{id}/ops/retry")
    public ApiResponse<Object> retryGatewaySubmission(@PathVariable Long id,
                                                      @Valid @RequestBody PaymentApprovalActionRequest request) {
        return ApiResponse.success(paymentService.retryGatewaySubmission(id, request));
    }
}
