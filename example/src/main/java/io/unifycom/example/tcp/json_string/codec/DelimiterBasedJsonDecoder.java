package io.unifycom.example.tcp.json_string.codec;

import io.unifycom.example.tcp.json_string.protocol.codec.InboundJsonToMessageDecoder;
import io.unifycom.netty.codec.DefaultNettyChannelDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.nio.charset.Charset;

public class DelimiterBasedJsonDecoder extends DefaultNettyChannelDecoder {

    private Charset charset;

    private final ByteBuf delimiter;
    private final int maxFrameLength;

    public DelimiterBasedJsonDecoder(int maxFrameLength, ByteBuf delimiter) {

        this.delimiter = delimiter;
        this.maxFrameLength = maxFrameLength;
    }

    public DelimiterBasedJsonDecoder(int maxFrameLength, String delimiter, Charset charset) {

        this(maxFrameLength, Unpooled.copiedBuffer(delimiter, charset));

        this.charset = charset;
    }

    @Override
    public ByteToMessageDecoder getByteToMessageDecoder() {

        return new DelimiterBasedFrameDecoder(maxFrameLength, delimiter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MessageToMessageDecoder<ByteBuf> getMessageToMessageDecoder() {

        return new InboundJsonToMessageDecoder(charset);
    }
}
