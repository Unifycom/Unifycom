package io.unifycom.netty.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DatagramPacketEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;

public abstract class AbstractNettyDatagramChannelEncoder<M> extends DefaultNettyChannelEncoder<M> {

    @Override
    public MessageToMessageEncoder getMessageToMessageEncoder() {

        return new DatagramPacketEncoder<>(new MessageToMessageEncoder<M>() {

            @Override
            protected void encode(ChannelHandlerContext ctx, M msg, List<Object> out) throws Exception {

                out.add(AbstractNettyDatagramChannelEncoder.this.encode(msg));
            }
        });
    }
}
