package io.unifycom.event.codec;

import io.unifycom.event.ChannelEvent;

public interface MessageToEventDecoder<T> {

    ChannelEvent decode(T message);
}
