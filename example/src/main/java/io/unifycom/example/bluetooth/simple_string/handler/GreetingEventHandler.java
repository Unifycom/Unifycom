package io.unifycom.example.bluetooth.simple_string.handler;

import io.unifycom.event.exception.UnsupportedEventException;
import io.unifycom.event.handler.AbstractChannelEventHandler;
import io.unifycom.event.result.ChannelEventResult;
import io.unifycom.example.bluetooth.simple_string.event.GreetingEvent;
import io.unifycom.example.bluetooth.simple_string.event.GreetingResult;

public class GreetingEventHandler extends AbstractChannelEventHandler<GreetingEvent> {

    @Override
    public ChannelEventResult onEvent0(GreetingEvent event) throws UnsupportedEventException {

        System.out.println("received: " + event.getStr1());
        return new GreetingResult("reply: " + event.getStr1());
    }
}
