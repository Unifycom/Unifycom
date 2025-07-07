package io.unifycom.example.tcp.binary_frame.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;
import io.unifycom.example.tcp.binary_frame.protocol.OutboundMessage;
import io.unifycom.socket.codec.DefaultSocketChannelEncoder;

public class TcpChannelOutboundEncoder extends DefaultSocketChannelEncoder {

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
