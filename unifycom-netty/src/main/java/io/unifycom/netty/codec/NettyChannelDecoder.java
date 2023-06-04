package io.unifycom.netty.codec;

import io.unifycom.codec.AbstractChannelDecoder;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;

public abstract class NettyChannelDecoder extends AbstractChannelDecoder {

    public abstract ByteToMessageDecoder getByteToMessageDecoder();

    public <I> MessageToMessageDecoder<I> getMessageToMessageDecoder() {

        return null;
    }
}
