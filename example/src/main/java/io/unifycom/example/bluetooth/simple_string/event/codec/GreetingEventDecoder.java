package io.unifycom.example.bluetooth.simple_string.event.codec;

import io.unifycom.event.ChannelEvent;
import io.unifycom.event.codec.MessageToEventDecoder;
import io.unifycom.example.bluetooth.simple_string.event.GreetingEvent;

public class GreetingEventDecoder implements MessageToEventDecoder<String> {

    @Override
    public ChannelEvent decode(String message) {

        GreetingEvent event = new GreetingEvent();
        event.setStr1(message);

        return event;
    }
}
