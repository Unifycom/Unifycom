package io.unifycom.example.tcp.binary_frame.protocol;

import io.netty.buffer.ByteBuf;
import java.io.IOException;

public interface OutboundMessage extends Message {

    ByteBuf bytes() throws IOException;
}
