package io.unifycom.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.SocketUtils;
import io.unifycom.Channel;
import io.unifycom.Envelope;
import io.unifycom.codec.AbstractChannelDecoder;
import io.unifycom.dispatch.ChannelDispatcher;
import io.unifycom.socket.client.AbstractSocketChannel;
import io.unifycom.socket.codec.SocketChannelDecoder;
import io.unifycom.socket.codec.SocketChannelEncoder;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpChannel extends AbstractSocketChannel {

    private static final Logger logger = LoggerFactory.getLogger(UdpChannel.class);

    protected static final EventLoopGroup WORKER_GROUP = Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(WORKER_GROUP::shutdownGracefully));
    }

    private SocketChannelDecoder channelDecoder;
    private SocketChannelEncoder<?> channelEncoder;

    public UdpChannel(UdpChannelConfig config, SocketChannelDecoder channelDecoder, SocketChannelEncoder<?> channelEncoder,
                      ChannelDispatcher channelDispatcher) {

        this.config = config;

        this.channelDecoder = channelDecoder;
        ((AbstractChannelDecoder)this.channelDecoder).setMaxInboundMessageSize(config.getMaxInboundMessageSize());

        this.channelEncoder = channelEncoder;
        this.channelDispatcher = channelDispatcher;

        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    @Override
    public synchronized Channel connect() {

        if (isActive()) {

            logger.warn("{} is active, don't connect it again.", getName());
            return this;
        }

        lock = new CountDownLatch(1);
        bootstrap = new Bootstrap();
        UdpChannelConfig config = (UdpChannelConfig)this.config;

        Class<? extends DatagramChannel> channelClass =
            (WORKER_GROUP instanceof EpollEventLoopGroup) ? EpollDatagramChannel.class : NioDatagramChannel.class;

        bootstrap.group(WORKER_GROUP).channel(channelClass).option(ChannelOption.SO_BROADCAST, true)
            .handler(new ChannelInitializer<DatagramChannel>() {

                @Override
                public void initChannel(DatagramChannel ch) {

                    ch.pipeline().addLast(new IdleStateHandler(0, 0, config.getPingIntervalSeconds()))
                        .addLast(channelDecoder.getMessageToMessageDecoder()).addLast(channelEncoder.getMessageToMessageEncoder())
                        .addLast(new UdpChannelInboundHandler(channelDispatcher, UdpChannel.this, ping));
                }
            });

        super.channel = bootstrap.bind(socketAddress(config)).channel();

        logger.info("{} is listening on {} ...... ", getName(), config.getConnectionString());

        return this;
    }

    @Override
    public Future<Void> send(Object out) throws IOException {

        if (out instanceof Envelope) {

            Envelope envelope = (Envelope)out;
            out = new DefaultAddressedEnvelope<>(envelope.getContent(), (SocketAddress)envelope.getRecipient(), (SocketAddress)envelope.getSender());
        }

        return super.send(out);
    }

    private static SocketAddress socketAddress(UdpChannelConfig config) {

        if (StringUtils.isEmpty(config.getHost())) {

            return new InetSocketAddress(config.getPort());
        }

        return SocketUtils.socketAddress(config.getHost(), config.getPort());
    }
}
