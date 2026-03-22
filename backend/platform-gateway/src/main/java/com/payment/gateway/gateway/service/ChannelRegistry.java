package com.payment.gateway.gateway.service;

import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.payment.gateway.common.channel.ChannelCatalogProvider;
import com.payment.gateway.common.exception.UnsupportedChannelException;
import com.payment.gateway.common.model.ChannelDescriptor;
import com.payment.gateway.gateway.service.channel.ChannelAdapter;
import org.springframework.stereotype.Component;

@Component
public class ChannelRegistry implements ChannelCatalogProvider {

    private final Map<String, ChannelAdapter> adapters;

    public ChannelRegistry(List<ChannelAdapter> adapters) {
        this.adapters = Collections.unmodifiableMap(adapters.stream()
                .collect(Collectors.toMap(ChannelAdapter::channelCode, Function.identity())));
    }

    public ChannelAdapter resolve(String channelCode) {
        ChannelAdapter adapter = adapters.get(channelCode);
        if (adapter == null) {
            throw new UnsupportedChannelException(channelCode);
        }
        return adapter;
    }

    @Override
    public List<ChannelDescriptor> getSupportedChannels() {
        return adapters.values().stream()
                .map(ChannelAdapter::descriptor)
                .sorted(Comparator.comparing(ChannelDescriptor::getCode))
                .collect(Collectors.toList());
    }
}
