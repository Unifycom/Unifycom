package io.unifycom.websocket.client;

import io.unifycom.AbstractChannel;
import io.unifycom.Channel;
import io.unifycom.dispatch.DefaultChannelDispatcher;
import io.unifycom.event.DisconnectedEvent;
import io.unifycom.websocket.event.codec.AbstractTextToEventDecoder;
import io.unifycom.websocket.event.codec.ResultToTextEncoder;
import io.undertow.connector.ByteBufferPool;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.client.WebSocketClient;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.IoFuture;
import org.xnio.IoFuture.Status;
import org.xnio.OptionMap;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

public class WebsocketChannel extends AbstractChannel {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketChannel.class);

    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private final String id = WebsocketChannel.class.getSimpleName() + "-" + COUNTER.getAndIncrement();

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(0, new BasicThreadFactory.Builder().namingPattern(
            WebsocketChannel.class.getSimpleName() + "-reconnect-%d").daemon(true).build());

    WebSocketChannel wsChannel;
    private IoFuture<WebSocketChannel> channelFuture;
    private ScheduledFuture<?> reconnectFuture;

    private WebsocketChannelConfig config;
    private AbstractTextToEventDecoder textToEventDecoder;

    public WebsocketChannel(WebsocketChannelConfig config, AbstractTextToEventDecoder textToEventDecoder) {

        this.config = config;
        this.channelDispatcher = new DefaultChannelDispatcher(textToEventDecoder, new ResultToTextEncoder());

        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    @Override
    public String getId() {

        return this.id;
    }

    @Override
    public synchronized void close() {

        DisconnectedEvent disconnectedEvent = null;

        if (reconnectFuture != null) {

            reconnectFuture.cancel(true);
        }

        if (wsChannel != null) {

            try {

                WebSockets.sendCloseBlocking(CloseMessage.NORMAL_CLOSURE, null, wsChannel);
                wsChannel.close();

                disconnectedEvent = new DisconnectedEvent(wsChannel.toString(), wsChannel.getSourceAddress().toString());
            } catch (IOException e) {

                logger.error("Closing " + getId() + " failed.", e);
            }
        }

        if (channelFuture != null) {

            Status status = channelFuture.cancel().await();
            logger.debug("Cancelled channel future, {}.", status.toString());
            channelFuture = null;
        }

        if (disconnectedEvent != null) {

            logger.debug("Connection of {} is inactive.", disconnectedEvent.getRemoteAddress());
            channelDispatcher.fire(this, disconnectedEvent);
        }

        wsChannel = null;

        logger.info("{}{} has been closed.", getId(), StringUtils.isNotBlank(getName()) ? ("[" + getName() + "]") : StringUtils.EMPTY);
    }

    @Override
    public boolean isClosed() {

        return channelFuture == null;
    }

    @Override
    public synchronized Channel connect() {

        if (isActive()) {

            logger.warn("{} is active, don't connect it again.", getId());
            return this;
        }

        URI uri = URI.create("ws://" + config.getConnectionString());

        ByteBufferPool bufferPool = new DefaultByteBufferPool(false, 1024);
        WebSocketClient.ConnectionBuilder connectionBuilder = WebSocketClient.connectionBuilder(getXnioWorker(), bufferPool, uri);

        channelFuture = connectionBuilder.connect();
        WebsocketHandlingNotifier handlingNotifier = new WebsocketHandlingNotifier(channelDispatcher, this);

        reconnectFuture = EXECUTOR.scheduleWithFixedDelay(() -> connect0(connectionBuilder, handlingNotifier, uri),
                config.getAutoConnectIntervalSeconds(), config.getAutoConnectIntervalSeconds(),
                TimeUnit.SECONDS);

        logger.info("{} client is connecting to {} ...... ", getId(), config.getConnectionString());

        return this;
    }

    private synchronized void connect0(WebSocketClient.ConnectionBuilder connectionBuilder, WebsocketHandlingNotifier handlingNotifier, URI uri) {

        if (isActive() || isClosed()) {

            return;
        }

        if (channelFuture.getStatus() == Status.WAITING) {

            return;
        }

        channelFuture.cancel().await();

        channelFuture = connectionBuilder.connect();
        channelFuture.addNotifier(handlingNotifier, uri);
        Status status = channelFuture.await();

        if (status != Status.DONE) {

            logger.warn("{} is not active, try again after {}s.", getId(), config.getAutoConnectIntervalSeconds());
        }
    }

    private XnioWorker getXnioWorker() throws RuntimeException {

        XnioWorker worker = null;

        try {

            worker = Xnio.getInstance().createWorker(OptionMap.EMPTY);
        } catch (IOException e) {

            throw new RuntimeException(e);
        }

        return worker;
    }

    @Override
    public boolean isActive() {

        return wsChannel != null && wsChannel.isOpen();
    }

    @Override
    public Channel blockUntilConnected() throws InterruptedException {

        if (isClosed()) {

            throw new InterruptedException("The channel could not be initialize, call connect() first.");
        }

        channelFuture.awaitInterruptibly();

        return this;
    }

    @Override
    public Channel blockUntilConnected(int timeout, TimeUnit timeUnit) throws InterruptedException {

        if (isClosed()) {

            throw new InterruptedException("The channel could not be initialize, call connect() first.");
        }

        channelFuture.awaitInterruptibly(timeout, timeUnit);

        return this;
    }

    @Override
    public Future<Void> send(Object out) throws IOException {

        if (!isActive()) {

            throw new IOException("The channel is not ready yet");
        }

        WebSockets.sendText(out.toString(), wsChannel, null);

        return null;
    }
}
