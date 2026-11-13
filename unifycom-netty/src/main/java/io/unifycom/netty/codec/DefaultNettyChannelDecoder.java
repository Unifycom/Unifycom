package io.unifycom.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.unifycom.codec.AbstractChannelDecoder;

public class DefaultNettyChannelDecoder extends AbstractChannelDecoder implements NettyChannelDecoder {

    @Override
    public <M> M decode(ByteBuf in) {

        return null;
    }

    public ByteToMessageDecoder getByteToMessageDecoder(){

        return null;
    }

    public <I> MessageToMessageDecoder<I> getMessageToMessageDecoder() {

        return null;
    }
}
