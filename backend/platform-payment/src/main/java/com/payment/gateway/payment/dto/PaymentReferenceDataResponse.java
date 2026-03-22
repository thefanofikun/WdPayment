package com.payment.gateway.payment.dto;

import java.util.List;

import com.payment.gateway.common.reference.model.MockBeneficiaryProfile;
import com.payment.gateway.common.reference.model.MockCustomerProfile;
import com.payment.gateway.common.reference.model.MockMerchantProfile;
import com.payment.gateway.common.reference.model.MockSourceAccountProfile;

public class PaymentReferenceDataResponse {

    private final List<MockMerchantProfile> merchants;
    private final List<MockCustomerProfile> customers;
    private final List<MockBeneficiaryProfile> beneficiaries;
    private final List<MockSourceAccountProfile> sourceAccounts;

    public PaymentReferenceDataResponse(List<MockMerchantProfile> merchants,
                                        List<MockCustomerProfile> customers,
                                        List<MockBeneficiaryProfile> beneficiaries,
                                        List<MockSourceAccountProfile> sourceAccounts) {
        this.merchants = merchants;
        this.customers = customers;
        this.beneficiaries = beneficiaries;
        this.sourceAccounts = sourceAccounts;
    }

    public List<MockMerchantProfile> getMerchants() {
        return merchants;
    }

    public List<MockCustomerProfile> getCustomers() {
        return customers;
    }

    public List<MockBeneficiaryProfile> getBeneficiaries() {
        return beneficiaries;
    }

    public List<MockSourceAccountProfile> getSourceAccounts() {
        return sourceAccounts;
    }
}
