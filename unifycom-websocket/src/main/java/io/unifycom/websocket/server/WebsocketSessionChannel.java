package io.unifycom.websocket.server;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.unifycom.AbstractChannel;
import io.unifycom.Channel;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WebsocketSessionChannel extends AbstractChannel {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketSessionChannel.class);

    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private final String id = WebsocketSessionChannel.class.getSimpleName() + "-" + COUNTER.getAndIncrement();

    private static final AttributeKey<String> ATTR_CHANNEL_NAME = AttributeKey.valueOf("X_ATTR.CHANNEL_NAME");

    private final io.netty.channel.Channel channel;

    public WebsocketSessionChannel(io.netty.channel.Channel channel) {

        this.channel = channel;
    }

    public io.netty.channel.Channel unwrap() {

        return this.channel;
    }

    @Override
    public String getName() {

        return Objects.toString(channel.attr(ATTR_CHANNEL_NAME).get(), StringUtils.EMPTY);
    }

    @Override
    public void setName(String name) {

        assert StringUtils.isNotBlank(name);
        channel.attr(ATTR_CHANNEL_NAME).set(name);
    }

    @Override
    public synchronized void close() {

        if (channel != null) {

            channel.close().syncUninterruptibly();
        }

        logger.info("{} has been closed.", getName());
    }

    @Override
    public boolean isClosed() {

        return channel == null || (!channel.isActive() && !channel.isOpen());
    }

    @Override
    public synchronized Channel connect() {

        if (channel != null) {

            SocketAddress address = channel.remoteAddress();
            logger.warn("{} of {} is client side connection, cannot connect it on server side.", getName(), address);
        }

        return this;
    }

    @Override
    public boolean isActive() {

        return channel != null && channel.isActive();
    }

    @Override
    public Future<Void> send(Object out) throws IOException {

        if (!isActive()) {

            throw new IOException("The channel is not ready yet");
        }

        return channel.writeAndFlush(new TextWebSocketFrame(out.toString()));
    }

    @Override
    public Channel blockUntilConnected() throws InterruptedException {

        throw new UnsupportedOperationException("The channel is created by client, this operation is unsupported on server side.");
    }

    @Override
    public Channel blockUntilConnected(int timeout, TimeUnit unit) throws InterruptedException {

        throw new UnsupportedOperationException("The channel is created by client, this operation is unsupported on server side.");
    }
}
