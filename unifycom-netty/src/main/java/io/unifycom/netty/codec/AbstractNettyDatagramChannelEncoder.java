package io.unifycom.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DatagramPacketEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractNettyDatagramChannelEncoder<M> extends DefaultNettyChannelEncoder<M> {

    @Override
    public MessageToMessageEncoder getMessageToMessageEncoder() {

        return new ByteToDatagramEncoder((p)->encode(p));
    }


    private class ByteToDatagramEncoder extends DatagramPacketEncoder<M> {

        public ByteToDatagramEncoder(Function<M, ByteBuf> fun) {

            super(new MessageToByteEncoder(fun));
        }
    }

    private class MessageToByteEncoder extends MessageToMessageEncoder<M> {


        private Function<M, ByteBuf> fun;

        public MessageToByteEncoder(Function<M, ByteBuf> fun) {

            this.fun = fun;
        }

        @Override
        protected void encode(ChannelHandlerContext ctx, M msg, List<Object> out) throws Exception {

            out.add(fun.apply(msg));
        }
    }
}
