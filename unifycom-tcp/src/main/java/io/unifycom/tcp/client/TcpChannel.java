package io.unifycom.tcp.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.unifycom.dispatch.Dispatcher;
import io.unifycom.netty.AbstractHostPortChannelConfig;
import io.unifycom.netty.client.AbstractSocketChannel;
import io.unifycom.netty.client.AbstractSocketChannelInboundHandler;
import io.unifycom.netty.client.AutoReconnectIdleStateHandler;
import io.unifycom.netty.codec.NettyChannelDecoder;
import io.unifycom.netty.codec.NettyChannelEncoder;
import io.unifycom.netty.codec.NettyCodecs;
import io.unifycom.netty.codec.ProxyProtocolV2Handler;

public class TcpChannel extends AbstractSocketChannel {

    public TcpChannel(TcpChannelConfig config, NettyChannelDecoder channelDecoder, NettyChannelEncoder<?> channelEncoder,
                      Dispatcher channelDispatcher) {

        super(config, channelDecoder, channelEncoder, channelDispatcher);
    }

    @Override
    protected void configureBootstrap(Bootstrap bootstrap) {

        TcpChannelConfig config = (TcpChannelConfig)this.config;

        bootstrap.remoteAddress(config.getHost(), config.getPort()).option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {

        TcpChannelConfig config = (TcpChannelConfig)this.config;

        pipeline.addLast(new ProxyProtocolV2Handler())
            .addLast(new AutoReconnectIdleStateHandler(0, 0, config.getPingIntervalSeconds(), config.getAutoConnectIntervalSeconds(),
                                                       this::connect0));

        NettyCodecs.addByteCodecHandlers(pipeline, channelEncoder, channelDecoder);

        pipeline.addLast(new AbstractSocketChannelInboundHandler(channelDispatcher, this, ping));
    }
}
