package io.unifycom.bluetooth.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.unifycom.dispatch.Dispatcher;
import io.unifycom.netty.channel.bluetooth.BluetoothDeviceAddress;
import io.unifycom.netty.channel.bluetooth.OioBluetoothChannel;
import io.unifycom.netty.client.AbstractSocketChannel;
import io.unifycom.netty.client.AbstractSocketChannelInboundHandler;
import io.unifycom.netty.client.AutoReconnectIdleStateHandler;
import io.unifycom.netty.codec.NettyChannelDecoder;
import io.unifycom.netty.codec.NettyChannelEncoder;
import io.unifycom.netty.codec.NettyCodecs;
import io.unifycom.netty.util.EventLoopGroups;

@SuppressWarnings("deprecation")
public class BluetoothChannel extends AbstractSocketChannel {

    private static final EventLoopGroup WORKER_GROUP = EventLoopGroups.newOioEventLoopGroup();

    static {
        EventLoopGroups.shutdownHook(WORKER_GROUP);
    }

    public BluetoothChannel(BluetoothChannelConfig config, NettyChannelDecoder channelDecoder, NettyChannelEncoder<?> channelEncoder,
                            Dispatcher channelDispatcher) {

        super(config, channelDecoder, channelEncoder, channelDispatcher);
    }

    @Override
    protected EventLoopGroup workerGroup() {

        return WORKER_GROUP;
    }

    @Override
    protected Class<? extends io.netty.channel.Channel> channelClass() {

        return OioBluetoothChannel.class;
    }

    @Override
    protected void configureBootstrap(Bootstrap bootstrap) {

        BluetoothChannelConfig config = (BluetoothChannelConfig)this.config;

        bootstrap.remoteAddress(new BluetoothDeviceAddress(config.getConnectionString()));
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {

        BluetoothChannelConfig config = (BluetoothChannelConfig)this.config;

        pipeline.addLast(new AutoReconnectIdleStateHandler(0, 0, config.getPingIntervalSeconds(), config.getAutoConnectIntervalSeconds(),
                                                           this::connect0));

        NettyCodecs.addByteCodecHandlers(pipeline, channelEncoder, channelDecoder);

        pipeline.addLast(new AbstractSocketChannelInboundHandler(channelDispatcher, this, ping));
    }
}
