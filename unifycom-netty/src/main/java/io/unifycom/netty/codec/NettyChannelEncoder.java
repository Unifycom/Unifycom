package io.unifycom.netty.codec;

import io.unifycom.codec.ChannelEncoder;
import io.netty.handler.codec.MessageToByteEncoder;

public abstract class NettyChannelEncoder<O> implements ChannelEncoder {

    public abstract MessageToByteEncoder<O> getMessageToByteEncoder();
}
