package io.unifycom.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.string.StringDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class DelimiterBasedStringDecoder extends DefaultNettyChannelDecoder {

    private Charset charset;

    private final ByteBuf delimiter;
    private final int maxFrameLength;

    public DelimiterBasedStringDecoder(int maxFrameLength, ByteBuf delimiter) {

        this.delimiter = delimiter;
        this.maxFrameLength = maxFrameLength;
    }

    public DelimiterBasedStringDecoder(int maxFrameLength, String delimiter) {

        this(maxFrameLength, delimiter, StandardCharsets.UTF_8);
    }

    public DelimiterBasedStringDecoder(int maxFrameLength, String delimiter, Charset charset) {

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

        return new StringDecoder(charset);
    }
}
