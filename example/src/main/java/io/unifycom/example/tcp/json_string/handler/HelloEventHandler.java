package io.unifycom.example.tcp.json_string.handler;

import io.unifycom.event.exception.UnsupportedEventException;
import io.unifycom.event.handler.AbstractChannelEventHandler;
import io.unifycom.event.result.ChannelEventResult;
import io.unifycom.example.tcp.json_string.event.HelloEvent;
import io.unifycom.example.tcp.json_string.event.HelloEventResult;


public class HelloEventHandler extends AbstractChannelEventHandler<HelloEvent> {

    @Override
    public ChannelEventResult onEvent0(HelloEvent event) throws UnsupportedEventException {

        System.out.println("received: " + event.getName() + ", " + event.getSex());
        return new HelloEventResult("reply: welcome " + event.getName());
    }
}
