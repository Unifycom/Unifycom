package io.unifycom.example.tcp.binary_frame.event.codec;

import io.unifycom.event.codec.ResultToMessageEncoder;
import io.unifycom.event.result.ChannelEventResult;
import io.unifycom.example.tcp.binary_frame.event.HelloEventResult;
import io.unifycom.example.tcp.binary_frame.protocol.OutboundHelloMessage;

public class MyResultToMessageEncoder implements ResultToMessageEncoder<OutboundHelloMessage> {

    @Override
    public OutboundHelloMessage encode(ChannelEventResult eventResult) {

        OutboundHelloMessage message = new OutboundHelloMessage();
        message.setPayload(((HelloEventResult)eventResult).getSay());

        return message;
    }
}
