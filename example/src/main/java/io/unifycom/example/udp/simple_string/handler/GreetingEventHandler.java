package io.unifycom.example.udp.simple_string.handler;

import io.unifycom.event.exception.UnsupportedEventException;
import io.unifycom.event.handler.AbstractChannelEventHandler;
import io.unifycom.event.result.ChannelEventResult;
import io.unifycom.example.udp.simple_string.event.GreetingEvent;
import io.unifycom.example.udp.simple_string.event.GreetingResult;


public class GreetingEventHandler extends AbstractChannelEventHandler<GreetingEvent> {

    @Override
    public ChannelEventResult onEvent0(GreetingEvent event) throws UnsupportedEventException {

        System.out.println("received: " + event);
        return new GreetingResult("reply: " + event);
    }
}
