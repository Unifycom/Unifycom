package io.unifycom.udp.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.unifycom.AbstractServerChannel;
import io.unifycom.Channel;
import io.unifycom.codec.AbstractChannelDecoder;
import io.unifycom.dispatch.Dispatcher;
import io.unifycom.netty.codec.NettyChannelDecoder;
import io.unifycom.netty.codec.NettyChannelEncoder;
import java.io.IOException;
import java.util.concurrent.Future;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpServerChannel extends AbstractServerChannel {

    private static final Logger logger = LoggerFactory.getLogger(UdpServerChannel.class);

    private UdpServerChannelConfig config;
    private NettyChannelDecoder channelDecoder;
    private NettyChannelEncoder<?> channelEncoder;

    private Bootstrap bootstrap;
    private EventLoopGroup workerGroup = Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
    private UdpChannelRegistry channelHolder = new UdpChannelRegistry();
    private io.netty.channel.Channel serverChannel;

    public UdpServerChannel(UdpServerChannelConfig config, NettyChannelDecoder channelDecoder, NettyChannelEncoder<?> channelEncoder,
                            Dispatcher channelDispatcher) {

        this.config = config;

        this.channelDecoder = channelDecoder;
        ((AbstractChannelDecoder)this.channelDecoder).setMaxInboundMessageSize(this.config.getMaxInboundMessageSize());

        this.channelEncoder = channelEncoder;
        this.channelDispatcher = channelDispatcher;

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    @Override
    public synchronized AbstractServerChannel startup() {

        if (bootstrap != null) {

            logger.warn("{} is listening on {}, don't start-up it again.", getName(), config.getConnectionString());
            return this;
        }

        bootstrap = new Bootstrap();
        Class<? extends DatagramChannel> channelClass =
            (workerGroup instanceof EpollEventLoopGroup) ? EpollDatagramChannel.class : NioDatagramChannel.class;

        bootstrap.group(workerGroup).channel(channelClass).option(ChannelOption.SO_BROADCAST, true)
            .handler(new ChannelInitializer<DatagramChannel>() {

                @Override
                public void initChannel(DatagramChannel ch) {

                    ch.pipeline().addLast(new IdleStateHandler(config.getReaderIdleTimeSeconds(), config.getWriterIdleTimeSeconds(),
                                      config.getAllIdleTimeSeconds()))
                        .addLast(channelEncoder.getMessageToMessageEncoder()).addLast(channelDecoder.getMessageToMessageDecoder())
                        .addLast(new UdpServerChannelInboundHandler(channelDispatcher, channelHolder));
                }
            });

        if (StringUtils.isEmpty(config.getHost())) {

            serverChannel = bootstrap.bind(config.getPort()).syncUninterruptibly().channel();
        } else {

            serverChannel = bootstrap.bind(config.getHost(), config.getPort()).syncUninterruptibly().channel();
        }

        logger.info("{} server is listening on {} ...... ", getName(), config.getConnectionString());

        return this;
    }

    @Override
    public boolean isReady() {

        return workerGroup != null && !workerGroup.isShutdown() && !workerGroup.isTerminated() && !workerGroup.isShuttingDown();
    }

    @Override
    public Future<Void> send(String channelName, Object out) throws IOException {

        Channel ch = getChannel(channelName);

        if (ch == null) {

            logger.error("Not found any channel by name {}.", channelName);
            return null;
        }

        return ch.send(out);
    }

    @Override
    public Channel getChannel(String channelName) {

        return channelHolder.getByName(channelName);
    }

    @Override
    public void shutdown() {

        channelHolder.all().forEach(Channel::close);

        if (serverChannel != null) {

            serverChannel.close().syncUninterruptibly();
            logger.debug("Server channel of {} on {} has been closed.", getName(), config.getPort());
        }

        if (workerGroup != null) {

            workerGroup.shutdownGracefully();
            logger.debug("WorkerGroup of {} on {} has been shutdown.", getName(), config.getPort());
        }

        if (channelDispatcher != null) {

            channelDispatcher.close();
            logger.info("Dispatcher of {} on {} has been shutdown gracefully.", getName(), config.getPort());
        }

        if (bootstrap != null) {

            bootstrap = null;
            logger.info("{} on {} has been shutdown gracefully.", getName(), config.getPort());
        }
    }
}
