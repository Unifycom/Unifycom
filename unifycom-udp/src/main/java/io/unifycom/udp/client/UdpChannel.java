package io.unifycom.udp.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.SocketUtils;
import io.unifycom.Envelope;
import io.unifycom.dispatch.Dispatcher;
import io.unifycom.netty.AbstractHostPortChannelConfig;
import io.unifycom.netty.client.AbstractSocketChannel;
import io.unifycom.netty.codec.NettyChannelDecoder;
import io.unifycom.netty.codec.NettyChannelEncoder;
import io.unifycom.netty.codec.NettyCodecs;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Future;
import org.apache.commons.lang3.StringUtils;

public class UdpChannel extends AbstractSocketChannel {

    public UdpChannel(UdpChannelConfig config, NettyChannelDecoder channelDecoder, NettyChannelEncoder<?> channelEncoder,
                      Dispatcher channelDispatcher) {

        super(config, channelDecoder, channelEncoder, channelDispatcher);
    }

    @Override
    protected Class<? extends io.netty.channel.Channel> channelClass() {

        return (workerGroup() instanceof EpollEventLoopGroup) ? EpollDatagramChannel.class : NioDatagramChannel.class;
    }

    @Override
    protected void configureBootstrap(Bootstrap bootstrap) {

        bootstrap.option(ChannelOption.SO_BROADCAST, true);
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {

        UdpChannelConfig config = (UdpChannelConfig)this.config;

        pipeline.addLast(new IdleStateHandler(0, 0, config.getPingIntervalSeconds()));

        NettyCodecs.addMessageCodecHandlers(pipeline, channelDecoder, channelEncoder);

        pipeline.addLast(new UdpChannelInboundHandler(channelDispatcher, this, ping));
    }

    /**
     * The udp channel is connectionless, it binds the local address instead of connecting to the remote peer.
     */
    @Override
    protected void doConnect() {

        UdpChannelConfig config = (UdpChannelConfig)this.config;

        channel = bootstrap.bind(bindAddress(config)).channel();
    }

    @Override
    public Future<Void> send(Object out) throws IOException {

        if (out instanceof Envelope) {

            Envelope envelope = (Envelope)out;
            out = new DefaultAddressedEnvelope<>(envelope.getContent(), (SocketAddress)envelope.getReceiver(), (SocketAddress)envelope.getSender());
        }

        return super.send(out);
    }

    private static SocketAddress bindAddress(UdpChannelConfig config) {

        if (StringUtils.isEmpty(config.getHost())) {

            return new InetSocketAddress(config.getPort());
        }

        return SocketUtils.socketAddress(config.getHost(), config.getPort());
    }
}
