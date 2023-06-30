package io.unifycom.example.tcp.simple_string.event.codec;

import io.unifycom.event.ChannelEvent;
import io.unifycom.event.codec.MessageToEventDecoder;
import io.unifycom.example.tcp.simple_string.event.HelloEvent;

public class MyMessageToEventDecoder implements MessageToEventDecoder<String> {

    @Override
    public ChannelEvent decode(String message) {

        HelloEvent event = new HelloEvent();
        event.setStr1(message);

        return event;
    }
}
