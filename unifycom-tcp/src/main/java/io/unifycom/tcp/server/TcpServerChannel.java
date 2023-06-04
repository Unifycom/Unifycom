package io.unifycom.tcp.server;

import io.unifycom.AbstractServerChannel;
import io.unifycom.Channel;
import io.unifycom.dispatch.ChannelDispatcher;
import io.unifycom.netty.codec.InboundProxy2Decoder;
import io.unifycom.netty.codec.NettyChannelDecoder;
import io.unifycom.netty.codec.NettyChannelEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class TcpServerChannel extends AbstractServerChannel {

    private static final Logger logger = LoggerFactory.getLogger(TcpServerChannel.class);

    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private final String id = TcpServerChannel.class.getSimpleName() + "-" + COUNTER.getAndIncrement();

    private TcpServerChannelConfig config;
    private NettyChannelDecoder channelDecoder;
    private NettyChannelEncoder<?> channelEncoder;

    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup = Epoll.isAvailable() ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
    private EventLoopGroup workerGroup = Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
    private TcpChannelGroup channelGroup = new TcpChannelGroup();

    public TcpServerChannel(TcpServerChannelConfig config, NettyChannelDecoder channelDecoder, NettyChannelEncoder<?> channelEncoder,
                            ChannelDispatcher channelDispatcher) {

        this.config = config;

        this.channelDecoder = channelDecoder;
        this.channelDecoder.setMaxInboundMessageSize(this.config.getMaxInboundMessageSize());

        this.channelEncoder = channelEncoder;
        this.channelDispatcher = channelDispatcher;

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    @Override
    public String getId() {

        return this.id;
    }

    @Override
    public synchronized AbstractServerChannel startup() {

        if (bootstrap != null) {

            logger.warn("{} is listening on {}, don't start-up it again.", getName(), config.getPort());
            return this;
        }

        bootstrap = new ServerBootstrap();
        Class<? extends ServerSocketChannel> channelClass = (workerGroup instanceof EpollEventLoopGroup) ? EpollServerSocketChannel.class : NioServerSocketChannel.class;

        bootstrap.group(bossGroup, workerGroup).channel(channelClass).childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {

                        ch.pipeline().addLast(new InboundProxy2Decoder()).addLast(
                                        new IdleStateHandler(config.getReaderIdleTimeSeconds(), config.getWriterIdleTimeSeconds(), config.getAllIdleTimeSeconds()))
                                .addLast(channelEncoder.getMessageToByteEncoder()).addLast(channelDecoder.getByteToMessageDecoder());

                        if (channelDecoder.getMessageToMessageDecoder() != null) {
                            ch.pipeline().addLast(channelDecoder.getMessageToMessageDecoder());
                        }

                        ch.pipeline().addLast(new TcpChannelInboundHandler(channelDispatcher, channelGroup));
                    }
                });

        if (StringUtils.isEmpty(config.getHost())) {
            bootstrap.bind(config.getPort());
        } else {
            bootstrap.bind(config.getHost(), config.getPort());
        }

        logger.info("{} server is listening on {} ...... ", getName(), config.getConnectionString());

        return this;
    }

    @Override
    public boolean isReady() {

        return bossGroup != null && !bossGroup.isShutdown() && !bossGroup.isTerminated() && !bossGroup.isShuttingDown();
    }

    @Override
    public Future<Void> send(String channelName, Object out) throws IOException {

        Channel ch = getClient(channelName);

        if (ch == null) {

            logger.error("Not found any channel by name {}.", channelName);
            return null;
        }

        return ch.send(out);
    }

    @Override
    public Channel getClient(String channelName) {

        return channelGroup.getByName(channelName);
    }

    @Override
    public void shutdown() {

        channelGroup.all().forEach(Channel::close);

        if (workerGroup != null) {

            workerGroup.shutdownGracefully();
            logger.debug("{}} of {} on {} has been shutdown.", workerGroup.getClass().getSimpleName(), getName(), config.getPort());
        }

        if (bossGroup != null) {

            bossGroup.shutdownGracefully();
            logger.debug("{}} of {} on {} has been shutdown.", bossGroup.getClass().getSimpleName(), getName(), config.getPort());
        }

        if (channelDispatcher != null) {

            channelDispatcher.close();
            logger.info("{}} of {} on {} has been shutdown gracefully.", channelDispatcher.getClass().getSimpleName(), getName(), config.getPort());
        }

        if (bootstrap != null) {

            bootstrap = null;
            logger.info("{} on {} has been shutdown gracefully.", getName(), config.getPort());
        }
    }
}
