package com.example.a2aaggregator.recipient;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;

@Component
public class RecipientGatewayFactory {
    private final EnumMap<RecipientType, RecipientGateway> gateways = new EnumMap<>(RecipientType.class);

    public RecipientGatewayFactory(List<RecipientGateway> gatewayList) {
        gatewayList.forEach(gateway -> gateways.put(gateway.type(), gateway));
    }

    public RecipientGateway get(RecipientType type) {
        var gateway = gateways.get(type);
        if (gateway == null) throw new IllegalArgumentException("Не поддержан тип получателя: " + type);
        return gateway;
    }
}
