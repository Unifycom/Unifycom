package io.unifycom.websocket.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * Maps an outbound message to a WebSocket frame: {@code byte[]}/{@code ByteBuf} go out as
 * {@link BinaryWebSocketFrame}, anything else as {@link TextWebSocketFrame}.
 */
public final class WebsocketFrames {

    private WebsocketFrames() {
    }

    public static WebSocketFrame toFrame(Object out) {

        if (out instanceof ByteBuf) {

            return new BinaryWebSocketFrame((ByteBuf)out);
        }

        if (out instanceof byte[]) {

            return new BinaryWebSocketFrame(Unpooled.wrappedBuffer((byte[])out));
        }

        return new TextWebSocketFrame(out.toString());
    }
}
