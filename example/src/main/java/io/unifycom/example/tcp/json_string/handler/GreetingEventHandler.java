package io.unifycom.example.tcp.json_string.handler;

import io.unifycom.event.exception.UnsupportedEventException;
import io.unifycom.event.handler.AbstractChannelEventHandler;
import io.unifycom.event.result.ChannelEventResult;
import io.unifycom.example.tcp.json_string.event.GreetingEvent;
import io.unifycom.example.tcp.json_string.event.GreetingResult;


public class GreetingEventHandler extends AbstractChannelEventHandler<GreetingEvent> {

    @Override
    public ChannelEventResult onEvent0(GreetingEvent event) throws UnsupportedEventException {

        System.out.println("received: " + event.getName() + ", " + event.getSex());
        return new GreetingResult("reply: welcome " + event.getName());
    }
}
