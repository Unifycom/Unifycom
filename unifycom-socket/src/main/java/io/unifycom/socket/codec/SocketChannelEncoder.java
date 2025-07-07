package io.unifycom.socket.codec;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.unifycom.codec.ChannelEncoder;

public interface SocketChannelEncoder<M> extends ChannelEncoder {

    ByteBuf encode(M out);

    MessageToByteEncoder<M> getMessageToByteEncoder();

    <I> MessageToMessageEncoder<I> getMessageToMessageEncoder();
}
