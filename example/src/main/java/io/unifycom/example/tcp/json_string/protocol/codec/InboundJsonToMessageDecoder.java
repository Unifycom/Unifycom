package io.unifycom.example.tcp.json_string.protocol.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.unifycom.example.tcp.json_string.protocol.InboundHelloMessage;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class InboundJsonToMessageDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final static ObjectMapper JSON = new ObjectMapper();

    private Charset charset = StandardCharsets.UTF_8;

    public InboundJsonToMessageDecoder() {

    }

    public InboundJsonToMessageDecoder(Charset charset) {

        this.charset = charset;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {

        String str = msg.toString(charset);
        InboundHelloMessage message = JSON.readValue(str, InboundHelloMessage.class);

        out.add(message);
    }
}
