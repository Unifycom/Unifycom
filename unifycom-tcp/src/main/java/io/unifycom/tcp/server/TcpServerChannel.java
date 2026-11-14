package io.unifycom.tcp.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.unifycom.dispatch.Dispatcher;
import io.unifycom.netty.codec.NettyChannelDecoder;
import io.unifycom.netty.codec.NettyChannelEncoder;
import io.unifycom.netty.codec.ProxyProtocolV2Handler;
import io.unifycom.netty.server.AbstractNettyChannelRegistry;
import io.unifycom.netty.server.AbstractNettyServerChannel;
import io.unifycom.netty.server.AbstractSocketChannelInboundHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class TcpServerChannel extends AbstractNettyServerChannel {

    public TcpServerChannel(TcpServerChannelConfig config, NettyChannelDecoder channelDecoder, NettyChannelEncoder<?> channelEncoder,
                            Dispatcher channelDispatcher) {

        super(config, channelDecoder, channelEncoder, new AbstractNettyChannelRegistry());

        this.channelDispatcher = channelDispatcher;
    }

    @Override
    protected void configureServerBootstrap(ServerBootstrap serverBootstrap) {

        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    }

    @Override
    protected void initChannel(io.netty.channel.Channel ch) {

        TcpServerChannelConfig config = (TcpServerChannelConfig)this.config;

        ch.pipeline().addLast(new ProxyProtocolV2Handler()).addLast(
                new IdleStateHandler(config.getReaderIdleTimeSeconds(), config.getWriterIdleTimeSeconds(), config.getAllIdleTimeSeconds()));

        addCodecHandlers(ch.pipeline());

        ch.pipeline().addLast(new AbstractSocketChannelInboundHandler(channelDispatcher, channelHolder));
    }
}
