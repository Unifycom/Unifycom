package io.unifycom.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringToByteEncoder extends NettyChannelEncoder<String> {

    private Charset charset;

    public StringToByteEncoder() {

        this(StandardCharsets.UTF_8);
    }

    public StringToByteEncoder(Charset charset) {

        this.charset = charset;
    }

    @Override
    public MessageToByteEncoder<String> getMessageToByteEncoder() {

        return new MessageToByteEncoder<String>() {

            @Override
            protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {

                ByteBuf encoded =  ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(msg), charset);
                out.writeBytes(encoded);

                encoded.release();
            }
        };
    }
}