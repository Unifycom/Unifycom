package io.unifycom.socket.codec;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;

public class DefaultSocketChannelEncoder<M> implements SocketChannelEncoder<M> {


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
