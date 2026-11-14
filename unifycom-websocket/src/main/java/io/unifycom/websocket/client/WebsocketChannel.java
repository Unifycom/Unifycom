package io.unifycom.websocket.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.unifycom.Channel;
import io.unifycom.dispatch.DefaultDispatcher;
import io.unifycom.netty.client.AbstractSocketChannel;
import io.unifycom.netty.client.AutoReconnectIdleStateHandler;
import io.unifycom.websocket.util.WebsocketFrames;
import io.unifycom.websocket.event.codec.AbstractBinaryToEventDecoder;
import io.unifycom.websocket.event.codec.AbstractTextToEventDecoder;
import io.unifycom.websocket.event.codec.WebsocketMessageToEventDecoder;
import io.unifycom.websocket.event.codec.ResultToTextEncoder;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketChannel extends AbstractSocketChannel {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketChannel.class);

    private static final int MAX_HTTP_CONTENT_LENGTH = 65536;

    volatile boolean handshaked;

    public WebsocketChannel(WebsocketChannelConfig config, AbstractTextToEventDecoder textToEventDecoder) {

        this(config, textToEventDecoder, null);
    }

    public WebsocketChannel(WebsocketChannelConfig config, AbstractTextToEventDecoder textToEventDecoder,
                            AbstractBinaryToEventDecoder binaryToEventDecoder) {

        super(config, null, null, null);

        this.channelDispatcher = new DefaultDispatcher(new WebsocketMessageToEventDecoder(textToEventDecoder, binaryToEventDecoder),
                                                       new ResultToTextEncoder());
    }

    @Override
    protected void beforeConnect() {

        handshaked = false;
    }

    @Override
    protected void configureBootstrap(Bootstrap bootstrap) {

        WebsocketChannelConfig config = (WebsocketChannelConfig)this.config;

        bootstrap.remoteAddress(config.getHost(), config.getPort())
            .option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true);
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {

        WebsocketChannelConfig config = (WebsocketChannelConfig)this.config;

        URI uri = URI.create("ws://" + config.getConnectionString());
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, true,
                                                                                             EmptyHttpHeaders.INSTANCE, config.getMaxFramePayloadLength());

        pipeline.addLast(new HttpClientCodec()).addLast(new HttpObjectAggregator(MAX_HTTP_CONTENT_LENGTH))
            .addLast(new AutoReconnectIdleStateHandler(0, 0, config.getPingIntervalSeconds(), config.getAutoConnectIntervalSeconds(),
                                                       this::connect0))
            .addLast(new WebSocketClientProtocolHandler(handshaker, true, false))
            .addLast(new WebSocketFrameAggregator(config.getMaxFramePayloadLength()))
            .addLast(new WebsocketChannelInboundHandler(channelDispatcher, this));
    }

    @Override
    protected synchronized void connect0() {

        if (isActive() || isClosed()) {

            return;
        }

        handshaked = false;

        bootstrap.connect().addListener((ChannelFutureListener)futureListener -> {

            if (futureListener.isSuccess()) {

                if (channel != null) {
                    channel.close();
                }

                channel = futureListener.channel();
            } else {

                logger.warn("{} is not active, try again after {}s. {}.", getName(), config.getAutoConnectIntervalSeconds(),
                            futureListener.cause().getMessage());
            }
        });
    }

    @Override
    public boolean isActive() {

        return super.isActive() && handshaked;
    }

    @Override
    public Future<Void> send(Object out) throws IOException {

        if (!isActive()) {

            throw new IOException("The channel is not ready yet");
        }

        return channel.writeAndFlush(WebsocketFrames.toFrame(out));
    }

    public URI getUri() {

        return URI.create("ws://" + ((WebsocketChannelConfig)config).getConnectionString());
    }

    void onHandshakeComplete() {

        handshaked = true;

        if (lock != null) {
            lock.countDown();
        }
    }

    public void blockUntilHandshaked() throws InterruptedException {

        if (isClosed()) {

            throw new InterruptedException("The channel could not be initialize, call connect() first.");
        }

        lock.await();
    }

    public void blockUntilHandshaked(int timeout, TimeUnit unit) throws InterruptedException {

        if (isClosed()) {

            throw new InterruptedException("The channel could not be initialize, call connect() first.");
        }

        lock.await(timeout, unit);
    }
}
