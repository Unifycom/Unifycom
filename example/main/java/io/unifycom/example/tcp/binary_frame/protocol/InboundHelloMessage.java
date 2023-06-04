package io.unifycom.example.tcp.binary_frame.protocol;

import io.unifycom.example.tcp.binary_frame.exception.UnsupportedProtocolException;
import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;

public class InboundHelloMessage implements InboundMessage {

    private int instruction;
    private int length;
    private int version;
    private long sequence;
    private long timestamp;
    private String payload;

    public InboundHelloMessage(ByteBuf buffer) throws UnsupportedProtocolException {

        int stx = buffer.readUnsignedByte();
        if (stx != STX) {
            throw new UnsupportedProtocolException(String.format("STX is expected %X, but %X.", STX, stx));
        }

        this.instruction = buffer.readUnsignedShort();
        this.length = buffer.readUnsignedShort();

        this.version = buffer.readUnsignedByte();
        this.sequence = buffer.readLong();
        this.timestamp = buffer.readLong();

        this.payload = buffer.readCharSequence(length - (LENGTH_OF_VER + LENGTH_OF_SEQ + LENGTH_OF_TIMESTAMP), StandardCharsets.UTF_8).toString();

        int etx = buffer.readUnsignedShort();
        if (etx != ETX) {
            throw new UnsupportedProtocolException(String.format("ETX is expected %X, but %X", ETX, etx));
        }
    }

    @Override
    public int getInstruction() {

        return instruction;
    }

    public int getLength() {

        return length;
    }

    public int getVersion() {

        return version;
    }

    public long getSequence() {

        return sequence;
    }

    public long getTimestamp() {

        return timestamp;
    }

    public String getPayload() {

        return payload;
    }
}
