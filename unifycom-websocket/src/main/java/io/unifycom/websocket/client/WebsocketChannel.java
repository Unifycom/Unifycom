package io.unifycom.websocket.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.timeout.IdleStateHandler;
import io.unifycom.Channel;
import io.unifycom.dispatch.DefaultDispatcher;
import io.unifycom.netty.client.AbstractSocketChannel;
import io.unifycom.websocket.event.codec.AbstractTextToEventDecoder;
import io.unifycom.websocket.event.codec.ResultToTextEncoder;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketChannel extends AbstractSocketChannel {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketChannel.class);

    protected static final EventLoopGroup WORKER_GROUP = Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(WORKER_GROUP::shutdownGracefully));
    }

    private static final int MAX_HTTP_CONTENT_LENGTH = 65536;
    private static final int MAX_FRAME_PAYLOAD_LENGTH = 65536;

    volatile boolean handshaked;

    private AbstractTextToEventDecoder textToEventDecoder;

    public WebsocketChannel(WebsocketChannelConfig config, AbstractTextToEventDecoder textToEventDecoder) {

        this.config = config;
        this.textToEventDecoder = textToEventDecoder;

        this.channelDispatcher = new DefaultDispatcher(textToEventDecoder, new ResultToTextEncoder());

        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    @Override
    public synchronized Channel connect() {

        if (isActive()) {

            logger.warn("{} is active, don't connect it again.", getName());
            return this;
        }

        handshaked = false;
        lock = new CountDownLatch(1);

        WebsocketChannelConfig config = (WebsocketChannelConfig)this.config;

        URI uri = URI.create("ws://" + config.getConnectionString());
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, true,
                                                                                             EmptyHttpHeaders.INSTANCE, MAX_FRAME_PAYLOAD_LENGTH);

        bootstrap = new Bootstrap();
        Class<? extends SocketChannel> channelClass =
                (WORKER_GROUP instanceof EpollEventLoopGroup) ? EpollSocketChannel.class : NioSocketChannel.class;

        bootstrap.group(WORKER_GROUP).channel(channelClass).remoteAddress(config.getHost(), config.getPort())
                .option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    public void initChannel(SocketChannel ch) {

                        ch.pipeline().addLast(new HttpClientCodec()).addLast(new HttpObjectAggregator(MAX_HTTP_CONTENT_LENGTH))
                            .addLast(new IdleStateHandler(0, 0, config.getPingIntervalSeconds()) {

                                @Override
                                public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

                                    ctx.channel().eventLoop().schedule(WebsocketChannel.this::connect0,
                                            config.getAutoConnectIntervalSeconds(), TimeUnit.SECONDS);
                                }
                            }).addLast(new WebSocketClientProtocolHandler(handshaker, true, false))
                            .addLast(new WebsocketChannelInboundHandler(channelDispatcher, WebsocketChannel.this));
                    }
                });

        this.connect0();

        logger.info("{} client is connecting to {} ...... ", getName(), config.getConnectionString());

        return this;
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

        return channel.writeAndFlush(new TextWebSocketFrame(out.toString()));
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
