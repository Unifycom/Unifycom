package io.unifycom.example.tcp.json_string.codec;

import com.alibaba.fastjson2.JSON;
import io.unifycom.netty.codec.NettyChannelEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class JsonMessageToByteEncoder extends NettyChannelEncoder<Object> {

    private Charset charset = StandardCharsets.UTF_8;

    public JsonMessageToByteEncoder() {

    }

    public JsonMessageToByteEncoder(Charset charset) {

        this.charset = charset;
    }

    @Override
    public MessageToByteEncoder<Object> getMessageToByteEncoder() {

        return new MessageToByteEncoder<Object>() {

            @Override
            protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {

                String str = JSON.toJSONString(msg);
                out.writeBytes(ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(str), charset));
            }
        };
    }
}
