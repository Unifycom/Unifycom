package io.unifycom.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.unifycom.codec.ChannelDecoder;

public interface NettyChannelDecoder extends ChannelDecoder {

    <M> M decode(ByteBuf in);

    ByteToMessageDecoder getByteToMessageDecoder();

    <I> MessageToMessageDecoder<I> getMessageToMessageDecoder();
}
