package io.unifycom.example.tcp.json_string.event.codec;

import io.unifycom.event.ChannelEvent;
import io.unifycom.event.codec.MessageToEventDecoder;
import io.unifycom.example.tcp.json_string.event.GreetingEvent;
import io.unifycom.example.tcp.json_string.protocol.InboundHelloMessage;

public class GreetingEventDecoder implements MessageToEventDecoder<InboundHelloMessage> {

    @Override
    public ChannelEvent decode(InboundHelloMessage message) {

        GreetingEvent event = new GreetingEvent();
        event.setName(message.getName());
        event.setSex(message.getSex());

        return event;
    }
}
