package io.unifycom.udp.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.unifycom.AbstractServerChannel;
import io.unifycom.dispatch.Dispatcher;
import io.unifycom.netty.codec.NettyChannelDecoder;
import io.unifycom.netty.codec.NettyChannelEncoder;
import io.unifycom.netty.codec.NettyCodecs;
import io.unifycom.netty.server.AbstractNettyChannelRegistry;
import io.unifycom.netty.server.AbstractNettyServerChannel;
import io.unifycom.netty.util.EventLoopGroups;
import java.net.InetSocketAddress;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The udp server channel, the datagram channel is connectionless so it is based on {@link Bootstrap} instead of
 * {@link io.netty.bootstrap.ServerBootstrap}, it overrides {@link #startup()} with its own binding flow but still
 * reuses the rest of the server skeleton.
 */
public class UdpServerChannel extends AbstractNettyServerChannel {

    private static final Logger logger = LoggerFactory.getLogger(UdpServerChannel.class);

    private Bootstrap datagramBootstrap;
    private io.netty.channel.Channel serverChannel;

    public UdpServerChannel(UdpServerChannelConfig config, NettyChannelDecoder channelDecoder, NettyChannelEncoder<?> channelEncoder,
                            Dispatcher channelDispatcher) {

        super(config, channelDecoder, channelEncoder, new AbstractNettyChannelRegistry());

        this.channelDispatcher = channelDispatcher;
    }

    @Override
    public synchronized AbstractServerChannel startup() {

        if (datagramBootstrap != null) {

            logger.warn("{} is listening on {}, don't start-up it again.", getName(), config.getConnectionString());
            return this;
        }

        datagramBootstrap = new Bootstrap();
        workerGroup = EventLoopGroups.newDefaultEventLoopGroup();

        UdpServerChannelConfig config = (UdpServerChannelConfig)this.config;

        Class<? extends DatagramChannel> channelClass =
            (workerGroup instanceof EpollEventLoopGroup) ? EpollDatagramChannel.class : NioDatagramChannel.class;

        datagramBootstrap.group(workerGroup).channel(channelClass).option(ChannelOption.SO_BROADCAST, true)
            .handler(new ChannelInitializer<DatagramChannel>() {

                @Override
                public void initChannel(DatagramChannel ch) {

                    ch.pipeline().addLast(new IdleStateHandler(config.getReaderIdleTimeSeconds(), config.getWriterIdleTimeSeconds(),
                                      config.getAllIdleTimeSeconds()));

                    NettyCodecs.addMessageCodecHandlers(ch.pipeline(), channelDecoder, channelEncoder);

                    ch.pipeline().addLast(new UdpServerChannelInboundHandler(channelDispatcher, channelHolder));
                }
            });

        if (StringUtils.isEmpty(config.getHost())) {

            serverChannel = datagramBootstrap.bind(config.getPort()).syncUninterruptibly().channel();
        } else {

            serverChannel = datagramBootstrap.bind(config.getHost(), config.getPort()).syncUninterruptibly().channel();
        }

        logger.info("{} server is listening on {} ...... ", getName(), config.getConnectionString());

        return this;
    }

    /**
     * The udp server channel is not based on ServerBootstrap, it assembles the pipeline of the datagram channel
     * in its own {@link #startup()} flow, so this hook is unsupported.
     */
    @Override
    protected void initChannel(io.netty.channel.Channel ch) {

        throw new UnsupportedOperationException("The udp server channel is not based on ServerBootstrap.");
    }

    @Override
    protected void beforeShutdown() {

        if (serverChannel != null) {

            serverChannel.close().syncUninterruptibly();
            logger.debug("Server channel of {} on {} has been closed.", getName(), config.getConnectionString());
        }
    }

    @Override
    public void shutdown() {

        super.shutdown();
        datagramBootstrap = null;
    }
}
