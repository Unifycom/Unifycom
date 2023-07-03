package io.unifycom.example.tcp.binary_frame.event.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.unifycom.event.ChannelEvent;
import io.unifycom.event.codec.MessageToEventDecoder;
import io.unifycom.example.tcp.binary_frame.event.HelloEvent;
import io.unifycom.example.tcp.binary_frame.protocol.InboundHelloMessage;

public class MyMessageToEventDecoder implements MessageToEventDecoder<InboundHelloMessage> {

    private final static ObjectMapper JSON = new ObjectMapper();

    @Override
    public ChannelEvent decode(InboundHelloMessage message) throws Exception {

        return JSON.readValue(message.getPayload(), HelloEvent.class);
    }
}
