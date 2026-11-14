package io.unifycom.websocket.event.codec;

import io.unifycom.event.ChannelEvent;
import io.unifycom.event.codec.MessageToEventDecoder;

/**
 * Decodes a binary WebSocket frame payload ({@code byte[]}) into a {@link ChannelEvent}.
 *
 * @see AbstractTextToEventDecoder
 */
public abstract class AbstractBinaryToEventDecoder implements MessageToEventDecoder<byte[]> {

    @Override
    public ChannelEvent decode(byte[] message) {

        return decode0(message);
    }

    public abstract ChannelEvent decode0(byte[] message);
}
