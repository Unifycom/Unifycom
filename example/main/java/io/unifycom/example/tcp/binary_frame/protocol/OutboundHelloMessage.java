package io.unifycom.example.tcp.binary_frame.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class OutboundHelloMessage implements OutboundMessage {

    private  static final PooledByteBufAllocator POOLED = PooledByteBufAllocator.DEFAULT;

    private int instruction;
    private int version;
    private long sequence;
    private long timestamp;
    private String payload;

    public int getInstruction() {

        return instruction;
    }

    public void setInstruction(int instruction) {

        this.instruction = instruction;
    }

    public int getVersion() {

        return version;
    }

    public void setVersion(int version) {

        this.version = version;
    }

    public long getSequence() {

        return sequence;
    }

    public void setSequence(long sequence) {

        this.sequence = sequence;
    }

    public long getTimestamp() {

        return timestamp;
    }

    public void setTimestamp(long timestamp) {

        this.timestamp = timestamp;
    }

    public String getPayload() {

        return payload;
    }

    public void setPayload(String payload) {

        this.payload = payload;
    }

    @Override
    public ByteBuf bytes() throws IOException {

        // 采用组合Buf，避免ByteBuf之间的拷贝
        CompositeByteBuf byteBuf = POOLED.compositeBuffer();

        ByteBuf headBuf = POOLED.buffer();
        headBuf.writeByte(STX);
        headBuf.writeShort(this.instruction);

        ByteBuf payloadBuf = POOLED.buffer();
        payloadBuf.writeCharSequence(payload, StandardCharsets.UTF_8);
        int length = (LENGTH_OF_VER + LENGTH_OF_SEQ + LENGTH_OF_TIMESTAMP) + (Objects.isNull(payload) ? 0 : payloadBuf.readableBytes());

        headBuf.writeShort(length);
        headBuf.writeByte(this.version);
        headBuf.writeLong(this.sequence);
        headBuf.writeLong(this.timestamp);

        byteBuf.addComponent(true, headBuf);
        byteBuf.addComponent(true, payloadBuf);

        ByteBuf tailBuf = POOLED.buffer();
        tailBuf.writeShort(ETX);
        byteBuf.addComponent(true, tailBuf);

        return byteBuf;
    }
}
