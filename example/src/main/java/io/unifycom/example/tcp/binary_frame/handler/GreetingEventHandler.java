package io.unifycom.example.tcp.binary_frame.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.unifycom.event.handler.AbstractChannelEventHandler;
import io.unifycom.event.result.ChannelEventResult;
import io.unifycom.example.tcp.binary_frame.event.GreetingEvent;
import io.unifycom.example.tcp.binary_frame.event.GreetingResult;


public class GreetingEventHandler extends AbstractChannelEventHandler<GreetingEvent> {

    private final static ObjectMapper JSON = new ObjectMapper();

    @Override
    public ChannelEventResult onEvent0(GreetingEvent event) throws Exception {

        System.out.println("received: " + JSON.writeValueAsString(event));
        return new GreetingResult("reply: welcome " + event.getName());
    }
}
