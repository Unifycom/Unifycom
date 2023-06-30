package io.unifycom.example.tcp.binary_frame.handler;

import com.alibaba.fastjson2.JSON;
import io.unifycom.event.exception.UnsupportedEventException;
import io.unifycom.event.handler.AbstractChannelEventHandler;
import io.unifycom.event.result.ChannelEventResult;
import io.unifycom.example.tcp.binary_frame.event.HelloEventResult;
import io.unifycom.example.tcp.binary_frame.event.HelloEvent;


public class HelloEventHandler extends AbstractChannelEventHandler<HelloEvent> {

    @Override
    public ChannelEventResult onEvent0(HelloEvent event) throws UnsupportedEventException {

        System.out.println("received: " + JSON.toJSONString(event));
        return new HelloEventResult("reply: welcome " + event.getName());
    }
}
