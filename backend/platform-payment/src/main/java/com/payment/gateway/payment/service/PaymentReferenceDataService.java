package com.payment.gateway.payment.service;

import java.util.Arrays;
import java.util.List;

import com.payment.gateway.common.reference.PaymentReferenceLookup;
import com.payment.gateway.common.reference.model.MockBeneficiaryProfile;
import com.payment.gateway.common.reference.model.MockCustomerProfile;
import com.payment.gateway.common.reference.model.MockMerchantProfile;
import com.payment.gateway.common.reference.model.MockSourceAccountProfile;
import com.payment.gateway.payment.dto.PaymentReferenceDataResponse;
import org.springframework.stereotype.Service;

@Service
public class PaymentReferenceDataService implements PaymentReferenceLookup {

    private final List<MockMerchantProfile> merchants = Arrays.asList(
            new MockMerchantProfile("MERCHANT-001", "Northstar Treasury Ltd", "Alice Wong", "USD"),
            new MockMerchantProfile("MERCHANT-002", "Bluewave Commerce LLC", "Daniel Tan", "EUR")
    );

    private final List<MockCustomerProfile> customers = Arrays.asList(
            new MockCustomerProfile("CUS-10001", "Northstar Operating Account", "MERCHANT-001"),
            new MockCustomerProfile("CUS-10002", "Northstar Client Funds", "MERCHANT-001"),
            new MockCustomerProfile("CUS-20001", "Bluewave Collections", "MERCHANT-002")
    );

    private final List<MockBeneficiaryProfile> beneficiaries = Arrays.asList(
            new MockBeneficiaryProfile("BEN-31001", "Oceanic Supplies Pte Ltd", "MERCHANT-001",
                    "SG", "7339", "1234567890", "OCBCSGSG", "OCBC Bank",
                    "63 Chulia Street, Singapore", "SINGAPORE", ""),
            new MockBeneficiaryProfile("BEN-31002", "Harbor Logistics Ltd", "MERCHANT-001",
                    "BH", "SGBDBHB2XXX", "20301000000056", "SGBDBHB2XXX", "Singapore Gulf Bank",
                    "5 Harbour Road, Manama, Bahrain", "MANAMA", ""),
            new MockBeneficiaryProfile("BEN-41001", "Euro Retail GmbH", "MERCHANT-002",
                    "DE", "DEUTDEFF", "DE12100100101234567895", "DEUTDEFF", "Deutsche Bank",
                    "Taunusanlage 12, Frankfurt, Germany", "FRANKFURT", "DE12100100101234567895")
    );

    private final List<MockSourceAccountProfile> sourceAccounts = Arrays.asList(
            new MockSourceAccountProfile("VA-90001", "MERCHANT-001", "USD", "Northstar Client Funds", "79401400000031"),
            new MockSourceAccountProfile("SETTLE-USD-01", "MERCHANT-001", "USD", "Northstar Settlement", "20301000004062"),
            new MockSourceAccountProfile("SETTLE-EUR-01", "MERCHANT-002", "EUR", "Bluewave Settlement", "20301000014014")
    );

    public PaymentReferenceDataResponse getReferenceData() {
        return new PaymentReferenceDataResponse(merchants, customers, beneficiaries, sourceAccounts);
    }

    @Override
    public MockMerchantProfile findMerchant(String merchantId) {
        for (MockMerchantProfile merchant : merchants) {
            if (merchant.getMerchantId().equals(merchantId)) {
                return merchant;
            }
        }
        return null;
    }

    @Override
    public MockCustomerProfile findCustomer(String customerReference) {
        for (MockCustomerProfile customer : customers) {
            if (customer.getCustomerReference().equals(customerReference)) {
                return customer;
            }
        }
        return null;
    }

    @Override
    public MockBeneficiaryProfile findBeneficiary(String beneficiaryReference) {
        if (beneficiaryReference == null || beneficiaryReference.trim().isEmpty()) {
            return null;
        }
        for (MockBeneficiaryProfile beneficiary : beneficiaries) {
            if (beneficiary.getBeneficiaryReference().equals(beneficiaryReference)) {
                return beneficiary;
            }
        }
        return null;
    }

    @Override
    public MockSourceAccountProfile findSourceAccount(String sourceAccountReference) {
        if (sourceAccountReference == null || sourceAccountReference.trim().isEmpty()) {
            return null;
        }
        for (MockSourceAccountProfile sourceAccount : sourceAccounts) {
            if (sourceAccount.getSourceAccountReference().equals(sourceAccountReference)) {
                return sourceAccount;
            }
        }
        return null;
    }
}
