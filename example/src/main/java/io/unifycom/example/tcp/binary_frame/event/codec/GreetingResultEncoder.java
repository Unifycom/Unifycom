package io.unifycom.example.tcp.binary_frame.event.codec;

import io.unifycom.event.codec.ResultToMessageEncoder;
import io.unifycom.event.result.ChannelEventResult;
import io.unifycom.example.tcp.binary_frame.event.GreetingResult;
import io.unifycom.example.tcp.binary_frame.protocol.OutboundHelloMessage;

public class GreetingResultEncoder implements ResultToMessageEncoder<OutboundHelloMessage> {

    @Override
    public OutboundHelloMessage encode(ChannelEventResult eventResult) {

        OutboundHelloMessage message = new OutboundHelloMessage();
        message.setPayload(((GreetingResult)eventResult).getSay());

        return message;
    }
}
