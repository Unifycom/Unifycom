package io.unifycom.example.tcp.json_string.event.codec;

import io.unifycom.event.ChannelEvent;
import io.unifycom.event.codec.MessageToEventDecoder;
import io.unifycom.example.tcp.json_string.event.HelloEvent;
import io.unifycom.example.tcp.json_string.protocol.InboundHelloMessage;

public class MyMessageToEventDecoder implements MessageToEventDecoder<InboundHelloMessage> {

    @Override
    public ChannelEvent decode(InboundHelloMessage message) {

        HelloEvent event = new HelloEvent();
        event.setName(message.getName());
        event.setSex(message.getSex());

        return event;
    }
}
