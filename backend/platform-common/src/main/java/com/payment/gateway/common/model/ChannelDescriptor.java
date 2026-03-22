package com.payment.gateway.common.model;

import java.util.List;

public class ChannelDescriptor {

    private final String code;
    private final String name;
    private final List<String> settlementRegions;
    private final List<String> capabilities;
    private final String notes;

    public ChannelDescriptor(String code, String name, List<String> settlementRegions,
                             List<String> capabilities, String notes) {
        this.code = code;
        this.name = name;
        this.settlementRegions = settlementRegions;
        this.capabilities = capabilities;
        this.notes = notes;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public List<String> getSettlementRegions() {
        return settlementRegions;
    }

    public List<String> getCapabilities() {
        return capabilities;
    }

    public String getNotes() {
        return notes;
    }
}
