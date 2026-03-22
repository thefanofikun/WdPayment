package com.payment.gateway.payment.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.payment.gateway.common.exception.PaymentStateException;
import com.payment.gateway.common.model.PayoutType;
import com.payment.gateway.common.reference.model.MockBeneficiaryProfile;
import com.payment.gateway.common.reference.model.MockCustomerProfile;
import com.payment.gateway.common.reference.model.MockMerchantProfile;
import com.payment.gateway.common.reference.model.MockSourceAccountProfile;
import com.payment.gateway.gateway.dto.request.PayoutRequest;
import com.payment.gateway.gateway.dto.request.VirtualAccountRequest;
import com.payment.gateway.gateway.model.GatewayExecutionResult;
import com.payment.gateway.gateway.service.ChannelGatewayService;
import com.payment.gateway.payment.dto.PaymentApprovalActionRequest;
import com.payment.gateway.payment.dto.PaymentCreateRequest;
import com.payment.gateway.payment.model.ApprovalDecision;
import com.payment.gateway.payment.model.ApprovalStage;
import com.payment.gateway.payment.model.PaymentApprovalRecord;
import com.payment.gateway.payment.model.PaymentDirection;
import com.payment.gateway.payment.model.PaymentEventRecord;
import com.payment.gateway.payment.model.PaymentEventType;
import com.payment.gateway.payment.model.PaymentMethod;
import com.payment.gateway.payment.model.PaymentOrder;
import com.payment.gateway.payment.model.PaymentStatus;
import com.payment.gateway.payment.repository.PaymentOrderRepository;
import com.payment.gateway.ops.service.ChannelRoutingService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentReferenceDataService paymentReferenceDataService;
    private final ChannelRoutingService channelRoutingService;
    private final ChannelGatewayService channelGatewayService;

    public PaymentService(PaymentOrderRepository paymentOrderRepository,
                          PaymentReferenceDataService paymentReferenceDataService,
                          ChannelRoutingService channelRoutingService,
                          ChannelGatewayService channelGatewayService) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentReferenceDataService = paymentReferenceDataService;
        this.channelRoutingService = channelRoutingService;
        this.channelGatewayService = channelGatewayService;
    }

    @Transactional
    public PaymentOrder createPayment(PaymentCreateRequest request) {
        PaymentOrder existing = paymentOrderRepository
                .findByMerchantIdAndIdempotencyKey(request.getMerchantId(), request.getIdempotencyKey())
                .orElse(null);
        if (existing != null) {
            return existing;
        }

        MockMerchantProfile merchant = paymentReferenceDataService.findMerchant(request.getMerchantId());
        MockCustomerProfile customer = paymentReferenceDataService.findCustomer(request.getCustomerReference());

        if (merchant == null) {
            throw new PaymentStateException("Unknown merchant: " + request.getMerchantId());
        }
        if (customer == null) {
            throw new PaymentStateException("Unknown customer reference: " + request.getCustomerReference());
        }
        if (!merchant.getMerchantId().equals(customer.getMerchantId())) {
            throw new PaymentStateException("Customer does not belong to merchant");
        }
        if (request.getDirection() == PaymentDirection.OUTBOUND
                && (request.getBeneficiaryReference() == null || request.getBeneficiaryReference().trim().isEmpty())) {
            throw new PaymentStateException("Outbound payment requires beneficiaryReference");
        }

        PaymentOrder order = new PaymentOrder();
        order.setPaymentNo(generatePaymentNo());
        order.setMerchantId(merchant.getMerchantId());
        order.setMerchantName(merchant.getMerchantName());
        order.setCustomerReference(customer.getCustomerReference());
        order.setCustomerName(customer.getCustomerName());
        order.setDirection(request.getDirection());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setAmount(request.getAmount());
        order.setCurrency(request.getCurrency());
        order.setBusinessReference(request.getBusinessReference());
        order.setIdempotencyKey(request.getIdempotencyKey());
        order.setSourceAccountReference(request.getSourceAccountReference());
        order.setBeneficiaryReference(request.getBeneficiaryReference());
        order.setRequestedChannelCode(request.getRequestedChannelCode());
        order.setNarrative(request.getNarrative());
        order.setPurposeCode(request.getPurposeCode());
        order.setCrmCaseId(valueOrDefault(request.getCrmCaseId(), "CRM-CASE-DEMO"));
        order.setSalesOwner(valueOrDefault(request.getSalesOwner(), "virtual.crm.owner"));
        order.setRelationshipManager(valueOrDefault(request.getRelationshipManager(), merchant.getRelationshipManager()));
        order.setStatus(PaymentStatus.PENDING_CHECKER_REVIEW);
        order.setVersion(1);
        order.setCreatedAt(Instant.now().toString());
        order.setUpdatedAt(order.getCreatedAt());
        order.getEvents().add(new PaymentEventRecord(
                PaymentEventType.CREATED,
                "system",
                "Payment order created",
                order.getCreatedAt()
        ));

        MockBeneficiaryProfile beneficiary = paymentReferenceDataService.findBeneficiary(request.getBeneficiaryReference());
        if (beneficiary != null) {
            order.setBeneficiaryName(beneficiary.getBeneficiaryName());
        }

        try {
            return paymentOrderRepository.save(order);
        } catch (DataIntegrityViolationException ex) {
            PaymentOrder duplicate = paymentOrderRepository
                    .findByMerchantIdAndIdempotencyKey(request.getMerchantId(), request.getIdempotencyKey())
                    .orElse(null);
            if (duplicate != null) {
                return duplicate;
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<PaymentOrder> listPayments(String merchantId, PaymentStatus status, PaymentDirection direction,
                                           String channelCode, String keyword) {
        List<PaymentOrder> items = paymentOrderRepository.findAllByOrderByIdDesc();
        List<PaymentOrder> filtered = new ArrayList<PaymentOrder>();
        for (PaymentOrder item : items) {
            if (!matches(item.getMerchantId(), merchantId)) {
                continue;
            }
            if (status != null && item.getStatus() != status) {
                continue;
            }
            if (direction != null && item.getDirection() != direction) {
                continue;
            }
            if (channelCode != null && channelCode.trim().length() > 0) {
                String requested = item.getRequestedChannelCode() == null ? "" : item.getRequestedChannelCode();
                String routed = item.getRoutedChannelCode() == null ? "" : item.getRoutedChannelCode();
                if (!channelCode.equalsIgnoreCase(requested) && !channelCode.equalsIgnoreCase(routed)) {
                    continue;
                }
            }
            if (!matchesKeyword(item, keyword)) {
                continue;
            }
            filtered.add(item);
        }
        return filtered;
    }

    @Transactional(readOnly = true)
    public PaymentOrder getPayment(Long id) {
        PaymentOrder order = paymentOrderRepository.findById(id).orElse(null);
        if (order == null) {
            throw new PaymentStateException("Payment order not found: " + id);
        }
        return order;
    }

    public PaymentOrder checkerApprove(Long id, PaymentApprovalActionRequest request) {
        return performApproval(id, ApprovalStage.CHECKER, ApprovalDecision.APPROVED, request);
    }

    public PaymentOrder checkerReject(Long id, PaymentApprovalActionRequest request) {
        return performApproval(id, ApprovalStage.CHECKER, ApprovalDecision.REJECTED, request);
    }

    public PaymentOrder l1Approve(Long id, PaymentApprovalActionRequest request) {
        return performApproval(id, ApprovalStage.L1, ApprovalDecision.APPROVED, request);
    }

    public PaymentOrder l1Reject(Long id, PaymentApprovalActionRequest request) {
        return performApproval(id, ApprovalStage.L1, ApprovalDecision.REJECTED, request);
    }

    public PaymentOrder l2Approve(Long id, PaymentApprovalActionRequest request) {
        return performApproval(id, ApprovalStage.L2, ApprovalDecision.APPROVED, request);
    }

    public PaymentOrder l2Reject(Long id, PaymentApprovalActionRequest request) {
        return performApproval(id, ApprovalStage.L2, ApprovalDecision.REJECTED, request);
    }

    public PaymentOrder cancelPayment(Long id, PaymentApprovalActionRequest request) {
        PaymentOrder order = getPayment(id);
        if (order.getStatus() != PaymentStatus.PENDING_CHECKER_REVIEW
                && order.getStatus() != PaymentStatus.PENDING_L1_REVIEW
                && order.getStatus() != PaymentStatus.PENDING_L2_REVIEW) {
            throw new PaymentStateException("Cancel is only allowed before gateway submission");
        }
        order.setStatus(PaymentStatus.CANCELLED);
        order.getEvents().add(new PaymentEventRecord(
                PaymentEventType.CANCELLED,
                request.getActor(),
                request.getComment(),
                Instant.now().toString()
        ));
        touch(order);
        return paymentOrderRepository.save(order);
    }

    public PaymentOrder markCompleted(Long id, PaymentApprovalActionRequest request) {
        PaymentOrder order = getPayment(id);
        if (order.getStatus() != PaymentStatus.GATEWAY_SUBMITTED && order.getStatus() != PaymentStatus.PROCESSING) {
            throw new PaymentStateException("Only submitted or processing orders can be marked completed");
        }
        order.setStatus(PaymentStatus.COMPLETED);
        order.getEvents().add(new PaymentEventRecord(
                PaymentEventType.MARKED_COMPLETED,
                request.getActor(),
                request.getComment(),
                Instant.now().toString()
        ));
        touch(order);
        return paymentOrderRepository.save(order);
    }

    public PaymentOrder markFailed(Long id, PaymentApprovalActionRequest request) {
        PaymentOrder order = getPayment(id);
        if (order.getStatus() != PaymentStatus.GATEWAY_SUBMITTED && order.getStatus() != PaymentStatus.PROCESSING) {
            throw new PaymentStateException("Only submitted or processing orders can be marked failed");
        }
        order.setStatus(PaymentStatus.FAILED);
        order.setGatewayMessage(valueOrDefault(request.getComment(), order.getGatewayMessage()));
        order.getEvents().add(new PaymentEventRecord(
                PaymentEventType.MARKED_FAILED,
                request.getActor(),
                request.getComment(),
                Instant.now().toString()
        ));
        touch(order);
        return paymentOrderRepository.save(order);
    }

    public PaymentOrder retryGatewaySubmission(Long id, PaymentApprovalActionRequest request) {
        PaymentOrder order = getPayment(id);
        if (order.getStatus() != PaymentStatus.FAILED) {
            throw new PaymentStateException("Only failed orders can be retried");
        }
        order.getEvents().add(new PaymentEventRecord(
                PaymentEventType.RETRY_SUBMISSION,
                request.getActor(),
                request.getComment(),
                Instant.now().toString()
        ));
        submitToGateway(order);
        touch(order);
        return paymentOrderRepository.save(order);
    }

    @Transactional
    private PaymentOrder performApproval(Long id, ApprovalStage stage, ApprovalDecision decision,
                                         PaymentApprovalActionRequest request) {
        PaymentOrder order = getPayment(id);
        validateTransition(order, stage);

        order.getApprovals().add(new PaymentApprovalRecord(
                stage,
                decision,
                request.getActor(),
                request.getComment(),
                Instant.now().toString()
        ));

        if (decision == ApprovalDecision.REJECTED) {
            order.setStatus(PaymentStatus.REJECTED);
            touch(order);
            return paymentOrderRepository.save(order);
        }

        if (stage == ApprovalStage.CHECKER) {
            order.setStatus(PaymentStatus.PENDING_L1_REVIEW);
        } else if (stage == ApprovalStage.L1) {
            order.setStatus(PaymentStatus.PENDING_L2_REVIEW);
        } else {
            submitToGateway(order);
        }

        touch(order);
        return paymentOrderRepository.save(order);
    }

    private void validateTransition(PaymentOrder order, ApprovalStage stage) {
        if (stage == ApprovalStage.CHECKER && order.getStatus() != PaymentStatus.PENDING_CHECKER_REVIEW) {
            throw new PaymentStateException("Checker can only act on PENDING_CHECKER_REVIEW");
        }
        if (stage == ApprovalStage.L1 && order.getStatus() != PaymentStatus.PENDING_L1_REVIEW) {
            throw new PaymentStateException("L1 can only act on PENDING_L1_REVIEW");
        }
        if (stage == ApprovalStage.L2 && order.getStatus() != PaymentStatus.PENDING_L2_REVIEW) {
            throw new PaymentStateException("L2 can only act on PENDING_L2_REVIEW");
        }
    }

    private void submitToGateway(PaymentOrder order) {
        if (order.getDirection() == PaymentDirection.OUTBOUND) {
            submitOutbound(order);
            return;
        }

        if (order.getPaymentMethod() == PaymentMethod.VIRTUAL_ACCOUNT) {
            submitInboundVirtualAccount(order);
            return;
        }

        order.setGatewayOperation("MANUAL_COLLECTION");
        order.setGatewayMessage("No direct gateway call for this inbound method yet");
        order.setStatus(PaymentStatus.PROCESSING);
    }

    private void submitOutbound(PaymentOrder order) {
        String channelCode = preferredOrRoutedChannel(order.getRequestedChannelCode(), "PAYOUT");
        MockSourceAccountProfile sourceAccount = paymentReferenceDataService.findSourceAccount(order.getSourceAccountReference());
        MockBeneficiaryProfile beneficiary = paymentReferenceDataService.findBeneficiary(order.getBeneficiaryReference());
        PayoutRequest gatewayRequest = new PayoutRequest();
        gatewayRequest.setChannelCode(channelCode);
        gatewayRequest.setMerchantId(order.getMerchantId());
        gatewayRequest.setPayoutReference(order.getPaymentNo());
        gatewayRequest.setAmount(order.getAmount());
        gatewayRequest.setCurrency(order.getCurrency());
        gatewayRequest.setSourceAccountReference(order.getSourceAccountReference());
        gatewayRequest.setBeneficiaryReference(order.getBeneficiaryReference());
        gatewayRequest.setNarrative(order.getNarrative());
        gatewayRequest.setPurposeCode(order.getPurposeCode());
        gatewayRequest.setValueDate(Instant.now().toString().substring(0, 10));
        gatewayRequest.setPayoutType(resolvePayoutType(order.getPaymentMethod()));
        gatewayRequest.setSourceAccountNumber(sourceAccount != null ? sourceAccount.getAccountNumber() : null);
        gatewayRequest.setSourceAccountName(sourceAccount != null ? sourceAccount.getAccountName() : order.getCustomerName());
        gatewayRequest.setUltimateSourceAccountNumber(sourceAccount != null && order.getPaymentMethod() == PaymentMethod.POBO
                ? sourceAccount.getAccountNumber()
                : null);
        gatewayRequest.setChargeBearer("OUR");
        gatewayRequest.setFeeCurrency(order.getCurrency());
        if (beneficiary != null) {
            gatewayRequest.setBeneficiaryAccountNumber(beneficiary.getAccountNumber());
            gatewayRequest.setBeneficiaryAccountName(beneficiary.getBeneficiaryName());
            gatewayRequest.setBeneficiaryBankCountry(beneficiary.getBankCountry());
            gatewayRequest.setBeneficiaryBankCode(beneficiary.getBankCode());
            gatewayRequest.setBeneficiarySwiftCode(beneficiary.getSwiftCode());
            gatewayRequest.setBeneficiaryBankName(beneficiary.getBankName());
            gatewayRequest.setBeneficiaryAddress(beneficiary.getAddress());
            gatewayRequest.setBeneficiaryCity(beneficiary.getCity());
        }

        GatewayExecutionResult result = channelGatewayService.createPayout(gatewayRequest);
        order.setRoutedChannelCode(channelCode);
        order.setGatewayOperation("PAYOUT");
        order.setGatewayRequestId(result.getRequestId());
        order.setGatewayMessage(result.getMessage());
        order.setStatus(result.isSuccess() ? PaymentStatus.GATEWAY_SUBMITTED : PaymentStatus.FAILED);
    }

    private void submitInboundVirtualAccount(PaymentOrder order) {
        String channelCode = preferredOrRoutedChannel(order.getRequestedChannelCode(), "VIRTUAL_ACCOUNT");
        MockSourceAccountProfile sourceAccount = paymentReferenceDataService.findSourceAccount(order.getSourceAccountReference());
        VirtualAccountRequest gatewayRequest = new VirtualAccountRequest();
        gatewayRequest.setChannelCode(channelCode);
        gatewayRequest.setMerchantId(order.getMerchantId());
        gatewayRequest.setCustomerReference(order.getCustomerReference());
        gatewayRequest.setVirtualAccountReference(order.getPaymentNo());
        gatewayRequest.setAccountName(order.getCustomerName());
        gatewayRequest.setCurrency(order.getCurrency());
        gatewayRequest.setCountry("SG");
        gatewayRequest.setBankCode("7339");
        gatewayRequest.setPurpose("Inbound collection");
        gatewayRequest.setMasterAccountNumber(sourceAccount != null ? sourceAccount.getAccountNumber() : "20301000004062");
        gatewayRequest.setMasterAccountCurrency(sourceAccount != null ? sourceAccount.getCurrency() : order.getCurrency());
        gatewayRequest.setCreateCount(1);
        gatewayRequest.setExternalRequestId(order.getPaymentNo());

        GatewayExecutionResult result = channelGatewayService.createVirtualAccount(gatewayRequest);
        order.setRoutedChannelCode(channelCode);
        order.setGatewayOperation("VIRTUAL_ACCOUNT");
        order.setGatewayRequestId(result.getRequestId());
        order.setGatewayMessage(result.getMessage());
        order.setStatus(result.isSuccess() ? PaymentStatus.GATEWAY_SUBMITTED : PaymentStatus.FAILED);
    }

    private String preferredOrRoutedChannel(String requestedChannelCode, String operation) {
        if (requestedChannelCode != null && requestedChannelCode.trim().length() > 0) {
            return requestedChannelCode;
        }
        String routedChannel = channelRoutingService.pickBestChannel(operation);
        if (routedChannel == null) {
            throw new PaymentStateException("No channel available for operation: " + operation);
        }
        return routedChannel;
    }

    private PayoutType resolvePayoutType(PaymentMethod paymentMethod) {
        if (paymentMethod == PaymentMethod.INTERNAL_TRANSFER) {
            return PayoutType.INTERNAL_TRANSFER;
        }
        if (paymentMethod == PaymentMethod.POBO) {
            return PayoutType.POBO;
        }
        return PayoutType.EXTERNAL_PAYOUT;
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    private boolean matches(String actual, String expected) {
        return expected == null || expected.trim().isEmpty() || expected.equalsIgnoreCase(actual);
    }

    private boolean matchesKeyword(PaymentOrder order, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        String probe = keyword.trim().toLowerCase();
        return contains(order.getPaymentNo(), probe)
                || contains(order.getBusinessReference(), probe)
                || contains(order.getIdempotencyKey(), probe)
                || contains(order.getCustomerReference(), probe)
                || contains(order.getMerchantId(), probe);
    }

    private boolean contains(String value, String probe) {
        return value != null && value.toLowerCase().contains(probe);
    }

    private void touch(PaymentOrder order) {
        order.setUpdatedAt(Instant.now().toString());
        order.setVersion(order.getVersion() + 1);
    }

    private String generatePaymentNo() {
        return "PAY-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
