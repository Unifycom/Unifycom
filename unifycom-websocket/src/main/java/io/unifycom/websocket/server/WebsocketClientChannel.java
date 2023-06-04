package io.unifycom.websocket.server;

import io.unifycom.AbstractChannel;
import io.unifycom.Channel;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WebsocketClientChannel extends AbstractChannel {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketClientChannel.class);

    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private final String id = WebsocketClientChannel.class.getSimpleName() + "-" + COUNTER.getAndIncrement();

    private final WebSocketChannel channel;

    private static final String ATTR_CHANNEL_NAME = "X_ATTR.CHANNEL_NAME";

    public WebsocketClientChannel(WebSocketChannel channel) {

        this.channel = channel;
    }

    public WebSocketChannel channel() {

        return this.channel;
    }

    @Override
    public String getId() {

        return this.id;
    }

    @Override
    public String getName() {

        return  Objects.toString(channel.getAttribute(ATTR_CHANNEL_NAME), StringUtils.EMPTY);
    }

    @Override
    public void setName(String name) {

        assert StringUtils.isNotBlank(name);
        channel.setAttribute(ATTR_CHANNEL_NAME, name);
    }

    @Override
    public synchronized void close() {

        if (channel != null) {

            try {

                channel.close();
            } catch (IOException e) {

                logger.warn(e.getLocalizedMessage(), e);
            }
        }

        logger.info("{}[{}] has been closed.", getName(), getId());
    }

    @Override
    public boolean isClosed() {

        return channel == null || !channel.isOpen();
    }

    @Override
    public synchronized Channel connect() {

        if (channel != null) {

            InetSocketAddress address = channel.getSourceAddress();
            logger.warn("{}[{}] of {}:{} is client side connection, cannot connect it on server side.", getId(), getName(), address.getHostString(),
                        address.getPort());
        }

        throw new UnsupportedOperationException("The channel is created by client, this operation is unsupported on server side.");
    }


    @Override
    public boolean isActive() {

        return channel != null && channel.isOpen();
    }

    @Override
    public Future<Void> send(Object out) throws IOException {

        if (!isActive()) {

            throw new IOException("The channel is not ready yet");
        }

        WebSockets.sendText(out.toString(), channel, null);

        return null;
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
