package io.unifycom.websocket.event.codec;

import io.unifycom.event.ChannelEvent;
import io.unifycom.event.codec.MessageToEventDecoder;

public abstract class AbstractTextToEventDecoder implements MessageToEventDecoder<String> {


    @Override
    public ChannelEvent decode(String message) {

        return decode0(message);
    }

    public abstract ChannelEvent decode0(String message);
}
