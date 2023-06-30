package io.unifycom.example.tcp.binary_frame.protocol;

public interface InboundMessage extends Message {

    int getInstruction();
}
