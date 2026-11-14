package io.unifycom.websocket.event.codec;

import io.unifycom.event.ChannelEvent;
import io.unifycom.event.codec.MessageToEventDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Routes a WebSocket message to the underlying decoder by its payload type: text messages
 * ({@link String}) go to the text decoder, binary messages ({@code byte[]}) go to the binary decoder.
 *
 * <p>If a binary message arrives but no binary decoder is registered, it falls back to the text
 * decoder as a UTF-8 string, which keeps the behavior compatible with releases that treated binary
 * frames as text.</p>
 */
public class WebsocketMessageToEventDecoder implements MessageToEventDecoder<Object> {

    private final MessageToEventDecoder<String> textToEventDecoder;
    private final MessageToEventDecoder<byte[]> binaryToEventDecoder;

    public WebsocketMessageToEventDecoder(MessageToEventDecoder<String> textToEventDecoder) {

        this(textToEventDecoder, null);
    }

    public WebsocketMessageToEventDecoder(MessageToEventDecoder<String> textToEventDecoder,
                                          MessageToEventDecoder<byte[]> binaryToEventDecoder) {

        if (textToEventDecoder == null && binaryToEventDecoder == null) {

            throw new IllegalArgumentException("At least one of textToEventDecoder and binaryToEventDecoder is required.");
        }

        this.textToEventDecoder = textToEventDecoder;
        this.binaryToEventDecoder = binaryToEventDecoder;
    }

    @Override
    public ChannelEvent decode(Object message) throws Exception {

        if (message instanceof byte[]) {

            return decodeBinary((byte[])message);
        }

        return decodeText((String)message);
    }

    private ChannelEvent decodeBinary(byte[] message) throws Exception {

        if (binaryToEventDecoder != null) {

            return binaryToEventDecoder.decode(message);
        }

        return textToEventDecoder.decode(new String(message, StandardCharsets.UTF_8));
    }

    private ChannelEvent decodeText(String message) throws Exception {

        if (textToEventDecoder != null) {

            return textToEventDecoder.decode(message);
        }

        throw new UnsupportedOperationException("No text decoder has been registered, cannot decode text message.");
    }
}
