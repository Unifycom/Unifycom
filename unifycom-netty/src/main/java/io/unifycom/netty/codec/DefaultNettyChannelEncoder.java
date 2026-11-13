package io.unifycom.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;

public class DefaultNettyChannelEncoder<M> implements NettyChannelEncoder<M> {


    @Override
    public ByteBuf encode(M out) {

        return null;
    }

    @Override
    public MessageToByteEncoder getMessageToByteEncoder() {

        return null;
    }

    @Override
    public MessageToMessageEncoder getMessageToMessageEncoder() {

        return null;
    }
}
