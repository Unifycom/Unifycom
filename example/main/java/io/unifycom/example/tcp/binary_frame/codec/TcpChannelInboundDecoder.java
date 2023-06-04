package io.unifycom.example.tcp.binary_frame.codec;

import io.unifycom.example.tcp.binary_frame.exception.UnsupportedProtocolException;
import io.unifycom.example.tcp.binary_frame.protocol.InboundHelloMessage;
import io.unifycom.example.tcp.binary_frame.protocol.Instruction;
import io.unifycom.example.tcp.binary_frame.protocol.Message;
import io.unifycom.netty.codec.NettyChannelDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.ReferenceCountUtil;

public class TcpChannelInboundDecoder extends NettyChannelDecoder {

    @Override
    public ByteToMessageDecoder getByteToMessageDecoder() {

        return new LengthFieldBasedFrameDecoder(Message.MAX_LENGTH, Message.LENGTH_OF_STX + Message.LENGTH_OF_INS,
                                                Message.LENGTH_OF_LEN, Message.LENGTH_OF_ETX, 0, true) {

            @Override
            protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {

                ByteBuf decoded = (ByteBuf)super.decode(ctx, in);

                if (decoded == null) {

                    return null;
                }

                try {

                    int instruction = decoded.getUnsignedShort(Message.LENGTH_OF_STX);

                    switch (instruction) {

                        case Instruction.INS_INBOUND_HELLO:
                            return new InboundHelloMessage(decoded);

                        default:
                            throw new UnsupportedProtocolException();
                    }
                } finally {

                    ReferenceCountUtil.release(decoded);
                }
            }

        };
    }
}
