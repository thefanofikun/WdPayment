package com.payment.gateway.common.channel;

import java.util.List;

import com.payment.gateway.common.model.ChannelDescriptor;

public interface ChannelCatalogProvider {

    List<ChannelDescriptor> getSupportedChannels();
}
