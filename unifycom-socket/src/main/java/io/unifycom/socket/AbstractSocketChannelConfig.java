package io.unifycom.socket;


import io.unifycom.AbstractChannelConfig;

public abstract class AbstractSocketChannelConfig extends AbstractChannelConfig {

    private int allIdleTimeSeconds = 0;
    private int readerIdleTimeSeconds = 0;
    private int writerIdleTimeSeconds = 0;

    private int maxInboundMessageSize = 0;

    public AbstractSocketChannelConfig() {

    }

    public AbstractSocketChannelConfig(String connectionString) {

        this.setConnectionString(connectionString);
    }

    public int getReaderIdleTimeSeconds() {

        return readerIdleTimeSeconds;
    }

    public void setReaderIdleTimeSeconds(int readerIdleTimeSeconds) {

        this.readerIdleTimeSeconds = readerIdleTimeSeconds;
    }

    public int getWriterIdleTimeSeconds() {

        return writerIdleTimeSeconds;
    }

    public void setWriterIdleTimeSeconds(int writerIdleTimeSeconds) {

        this.writerIdleTimeSeconds = writerIdleTimeSeconds;
    }

    public int getAllIdleTimeSeconds() {

        return allIdleTimeSeconds;
    }

    public void setAllIdleTimeSeconds(int allIdleTimeSeconds) {

        this.allIdleTimeSeconds = allIdleTimeSeconds;
    }

    public int getMaxInboundMessageSize() {

        return maxInboundMessageSize;
    }

    public void setMaxInboundMessageSize(int maxInboundMessageSize) {

        this.maxInboundMessageSize = maxInboundMessageSize;
    }

    @Override
    public String toString() {

        return String.format("allIdleTimeSeconds = %s, readerIdleTimeSeconds = %s, writerIdleTimeSeconds = %s, maxInboundMessageSize = %s, %s",
                allIdleTimeSeconds, readerIdleTimeSeconds, writerIdleTimeSeconds, maxInboundMessageSize, super.toString());
    }
}
