package io.unifycom.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.unifycom.Envelope;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractNettyDatagramChannelDecoder<M> extends DefaultNettyChannelDecoder {

    @Override
    public MessageToMessageDecoder<DatagramPacket> getMessageToMessageDecoder() {

        return new ByteToMessageDecoder((p) -> decode(p));
    }

    private class ByteToMessageDecoder extends MessageToMessageDecoder<DatagramPacket> {

        private Function<ByteBuf, M> fun;

        public ByteToMessageDecoder(Function<ByteBuf, M> fun) {

            this.fun = fun;
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {

            out.add(new Envelope<>(fun.apply(msg.content()), msg.recipient(), msg.sender()));
        }
    }
}
