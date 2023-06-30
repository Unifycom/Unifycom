package io.unifycom.netty.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.unifycom.Envelope;
import java.util.List;

public abstract class AbstractNettyDatagramChannelDecoder<M> extends DefaultNettyChannelDecoder {

    @Override
    public MessageToMessageDecoder<DatagramPacket> getMessageToMessageDecoder() {

        return new MessageToMessageDecoder<DatagramPacket>() {

            @Override
            protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {

                out.add(new Envelope<>(AbstractNettyDatagramChannelDecoder.this.decode(msg.content()), msg.recipient(), msg.sender()));
            }
        };
    }
}
