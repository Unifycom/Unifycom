package io.unifycom.example.tcp.json_string.event.codec;

import io.unifycom.event.codec.ResultToMessageEncoder;
import io.unifycom.event.result.ChannelEventResult;
import io.unifycom.example.tcp.json_string.protocol.OutboundHelloMessage;
import io.unifycom.example.tcp.json_string.event.GreetingResult;

public class GreetingResultEncoder implements ResultToMessageEncoder<OutboundHelloMessage> {

    @Override
    public OutboundHelloMessage encode(ChannelEventResult eventResult) {

        OutboundHelloMessage message = new OutboundHelloMessage();
        message.setSay(((GreetingResult)eventResult).getSay());

        return message;
    }
}
