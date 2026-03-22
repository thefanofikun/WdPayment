package com.payment.gateway.common.reference.model;

public class MockBeneficiaryProfile {

    private final String beneficiaryReference;
    private final String beneficiaryName;
    private final String merchantId;
    private final String bankCountry;
    private final String bankCode;
    private final String accountNumber;
    private final String swiftCode;
    private final String bankName;
    private final String address;
    private final String city;
    private final String iban;

    public MockBeneficiaryProfile(String beneficiaryReference, String beneficiaryName, String merchantId,
                                  String bankCountry, String bankCode, String accountNumber, String swiftCode,
                                  String bankName, String address, String city, String iban) {
        this.beneficiaryReference = beneficiaryReference;
        this.beneficiaryName = beneficiaryName;
        this.merchantId = merchantId;
        this.bankCountry = bankCountry;
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.swiftCode = swiftCode;
        this.bankName = bankName;
        this.address = address;
        this.city = city;
        this.iban = iban;
    }

    public String getBeneficiaryReference() {
        return beneficiaryReference;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getBankCountry() {
        return bankCountry;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getSwiftCode() {
        return swiftCode;
    }

    public String getBankName() {
        return bankName;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getIban() {
        return iban;
    }
}
