package io.unifycom.example.tcp.binary_frame.codec;

import io.unifycom.example.tcp.binary_frame.protocol.OutboundMessage;
import io.unifycom.netty.codec.NettyChannelEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;

public class TcpChannelOutboundEncoder extends NettyChannelEncoder {

    @Override
    public MessageToByteEncoder getMessageToByteEncoder() {

        return new MessageToByteEncoder<OutboundMessage>() {


            @Override
            protected void encode(ChannelHandlerContext ctx, OutboundMessage msg, ByteBuf out) throws Exception {

                ByteBuf encoded = null;

                try {

                    encoded = msg.bytes();
                    out.writeBytes(encoded);
                } finally {

                    if (encoded != null) {

                        ReferenceCountUtil.release(encoded);
                    }
                }
            }
        };
    }
}
