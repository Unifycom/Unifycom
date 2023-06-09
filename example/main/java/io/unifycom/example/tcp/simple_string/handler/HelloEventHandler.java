package io.unifycom.example.tcp.simple_string.handler;

import io.unifycom.event.exception.UnsupportedEventException;
import io.unifycom.event.handler.AbstractChannelEventHandler;
import io.unifycom.event.result.ChannelEventResult;
import io.unifycom.example.tcp.simple_string.event.HelloEvent;
import io.unifycom.example.tcp.simple_string.event.HelloEventResult;


public class HelloEventHandler extends AbstractChannelEventHandler<HelloEvent> {

    @Override
    public ChannelEventResult onEvent0(HelloEvent event) throws UnsupportedEventException {

        System.out.println("received: " + event.getStr1());
        return new HelloEventResult("reply: " + event.getStr1());
    }
}
