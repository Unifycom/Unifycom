package io.unifycom.websocket.event.codec;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.unifycom.event.ChannelEvent;
import io.unifycom.websocket.util.WebsocketFrames;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class WebsocketMessageToEventDecoderTest {

    private static class TextEvent implements ChannelEvent {
    }

    private static class BinaryEvent implements ChannelEvent {
    }

    private static class MessageEvent implements ChannelEvent {
    }

    private final AbstractTextToEventDecoder textDecoder = new AbstractTextToEventDecoder() {

        @Override
        public ChannelEvent decode0(String message) {

            return "text".equals(message) ? new TextEvent() : new MessageEvent();
        }
    };

    private final AbstractBinaryToEventDecoder binaryDecoder = new AbstractBinaryToEventDecoder() {

        @Override
        public ChannelEvent decode0(byte[] message) {

            return new BinaryEvent();
        }
    };

    @Test
    public void testRoutesTextMessageToTextDecoder() throws Exception {

        WebsocketMessageToEventDecoder decoder = new WebsocketMessageToEventDecoder(textDecoder, binaryDecoder);

        assertSame(TextEvent.class, decoder.decode("text").getClass());
        assertSame(MessageEvent.class, decoder.decode("any").getClass());
    }

    @Test
    public void testRoutesBinaryMessageToBinaryDecoder() throws Exception {

        WebsocketMessageToEventDecoder decoder = new WebsocketMessageToEventDecoder(textDecoder, binaryDecoder);

        assertSame(BinaryEvent.class, decoder.decode(new byte[] {1, 2, 3}).getClass());
    }

    @Test
    public void testBinaryMessageFallsBackToTextDecoderAsUtf8() throws Exception {

        WebsocketMessageToEventDecoder decoder = new WebsocketMessageToEventDecoder(textDecoder, null);

        assertSame(MessageEvent.class, decoder.decode("any".getBytes(StandardCharsets.UTF_8)).getClass());
    }

    @Test
    public void testBinaryOnlyDecoder() throws Exception {

        WebsocketMessageToEventDecoder decoder = new WebsocketMessageToEventDecoder(null, binaryDecoder);

        assertSame(BinaryEvent.class, decoder.decode(new byte[] {1}).getClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBothDecodersNullIsRejected() {

        new WebsocketMessageToEventDecoder(null, null);
    }

    @Test
    public void testByteArrayOutGoesToBinaryFrame() {

        byte[] payload = {1, 2, 3};

        BinaryWebSocketFrame frame = (BinaryWebSocketFrame)WebsocketFrames.toFrame(payload);
        assertArrayEquals(payload, toArray(frame.content()));
    }

    @Test
    public void testByteBufOutGoesToBinaryFrame() {

        ByteBuf payload = Unpooled.wrappedBuffer(new byte[] {4, 5});

        BinaryWebSocketFrame frame = (BinaryWebSocketFrame)WebsocketFrames.toFrame(payload);
        assertArrayEquals(new byte[] {4, 5}, toArray(frame.content()));
    }

    @Test
    public void testStringOutGoesToTextFrame() {

        TextWebSocketFrame frame = (TextWebSocketFrame)WebsocketFrames.toFrame("hello");
        assertEquals("hello", frame.text());
    }

    @Test
    public void testOtherObjectOutGoesToTextFrame() {

        TextWebSocketFrame frame = (TextWebSocketFrame)WebsocketFrames.toFrame(new Object() {

            @Override
            public String toString() {

                return "as-text";
            }
        });

        assertEquals("as-text", frame.text());
    }

    private static byte[] toArray(ByteBuf content) {

        byte[] data = new byte[content.readableBytes()];
        content.readBytes(data);

        return data;
    }
}
