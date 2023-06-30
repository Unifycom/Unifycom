package io.unifycom.tcp.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
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
import io.netty.handler.timeout.IdleStateHandler;
import io.unifycom.Channel;
import io.unifycom.codec.AbstractChannelDecoder;
import io.unifycom.dispatch.ChannelDispatcher;
import io.unifycom.netty.client.AbstractNettyChannel;
import io.unifycom.netty.codec.InboundProxy2Decoder;
import io.unifycom.netty.codec.NettyChannelDecoder;
import io.unifycom.netty.codec.NettyChannelEncoder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpChannel extends AbstractNettyChannel {

    private static final Logger logger = LoggerFactory.getLogger(TcpChannel.class);

    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private final String id = TcpChannel.class.getSimpleName() + "-" + COUNTER.getAndIncrement();

    protected static final EventLoopGroup WORKER_GROUP = Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(WORKER_GROUP::shutdownGracefully));
    }

    private NettyChannelDecoder channelDecoder;
    private NettyChannelEncoder<?> channelEncoder;

    public TcpChannel(TcpChannelConfig config, NettyChannelDecoder channelDecoder, NettyChannelEncoder<?> channelEncoder,
                      ChannelDispatcher channelDispatcher) {

        this.config = config;

        this.channelDecoder = channelDecoder;
        ((AbstractChannelDecoder)this.channelDecoder).setMaxInboundMessageSize(config.getMaxInboundMessageSize());

        this.channelEncoder = channelEncoder;
        this.channelDispatcher = channelDispatcher;

        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    @Override
    public String getId() {

        return this.id;
    }


    @Override
    public synchronized Channel connect() {

        if (isActive()) {

            logger.warn("{} is active, don't connect it again.", getId());
            return this;
        }

        lock = new CountDownLatch(1);
        bootstrap = new Bootstrap();
        TcpChannelConfig config = (TcpChannelConfig)this.config;

        Class<? extends SocketChannel> channelClass =
            (WORKER_GROUP instanceof EpollEventLoopGroup) ? EpollSocketChannel.class : NioSocketChannel.class;

        bootstrap.group(WORKER_GROUP).channel(channelClass).remoteAddress(config.getHost(), config.getPort()).option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .handler(new ChannelInitializer<SocketChannel>() {

                @Override
                public void initChannel(SocketChannel ch) {

                    ch.pipeline().addLast(new InboundProxy2Decoder()).addLast(new IdleStateHandler(0, 0, config.getPingIntervalSeconds()) {

                        @Override
                        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

                            ctx.channel().eventLoop().schedule(TcpChannel.this::connect0, config.getAutoConnectIntervalSeconds(), TimeUnit.SECONDS);
                        }
                    }).addLast(channelEncoder.getMessageToByteEncoder()).addLast(channelDecoder.getByteToMessageDecoder());

                    if (channelDecoder.getMessageToMessageDecoder() != null) {

                        ch.pipeline().addLast(channelDecoder.getMessageToMessageDecoder());
                    }

                    ch.pipeline().addLast(new TcpChannelInboundHandler(channelDispatcher, TcpChannel.this, ping));
                }
            });

        this.connect0();

        logger.info("{} client is connecting to {} ...... ", getId(), config.getConnectionString());

        return this;
    }
}
