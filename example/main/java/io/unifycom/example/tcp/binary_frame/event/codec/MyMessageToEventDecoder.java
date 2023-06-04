package io.unifycom.example.tcp.binary_frame.event.codec;

import com.alibaba.fastjson2.JSON;
import io.unifycom.event.ChannelEvent;
import io.unifycom.event.codec.MessageToEventDecoder;
import io.unifycom.example.tcp.binary_frame.event.HelloEvent;
import io.unifycom.example.tcp.binary_frame.protocol.InboundHelloMessage;

public class MyMessageToEventDecoder implements MessageToEventDecoder<InboundHelloMessage> {

    @Override
    public ChannelEvent decode(InboundHelloMessage message) {

        return JSON.parseObject(message.getPayload(), HelloEvent.class);
    }
}
