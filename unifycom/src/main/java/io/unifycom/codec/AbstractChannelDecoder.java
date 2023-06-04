package io.unifycom.codec;

public class AbstractChannelDecoder implements ChannelDecoder {

    private int maxInboundMessageSize;

    public int getMaxInboundMessageSize() {

        return maxInboundMessageSize;
    }

    public void setMaxInboundMessageSize(int maxInboundMessageSize) {

        this.maxInboundMessageSize = maxInboundMessageSize;
    }
}
