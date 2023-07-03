package io.unifycom.example.tcp.binary_frame.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.unifycom.event.handler.AbstractChannelEventHandler;
import io.unifycom.event.result.ChannelEventResult;
import io.unifycom.example.tcp.binary_frame.event.HelloEvent;
import io.unifycom.example.tcp.binary_frame.event.HelloEventResult;


public class HelloEventHandler extends AbstractChannelEventHandler<HelloEvent> {

    private final static ObjectMapper JSON = new ObjectMapper();

    @Override
    public ChannelEventResult onEvent0(HelloEvent event) throws Exception {

        System.out.println("received: " + JSON.writeValueAsString(event));
        return new HelloEventResult("reply: welcome " + event.getName());
    }
}
