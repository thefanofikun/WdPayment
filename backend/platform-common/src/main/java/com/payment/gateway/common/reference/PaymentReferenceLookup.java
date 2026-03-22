package com.payment.gateway.common.reference;

import com.payment.gateway.common.reference.model.MockBeneficiaryProfile;
import com.payment.gateway.common.reference.model.MockCustomerProfile;
import com.payment.gateway.common.reference.model.MockMerchantProfile;
import com.payment.gateway.common.reference.model.MockSourceAccountProfile;

public interface PaymentReferenceLookup {

    MockMerchantProfile findMerchant(String merchantId);

    MockCustomerProfile findCustomer(String customerReference);

    MockBeneficiaryProfile findBeneficiary(String beneficiaryReference);

    MockSourceAccountProfile findSourceAccount(String sourceAccountReference);
}
