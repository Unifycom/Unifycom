package io.unifycom.rxtx;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.unifycom.dispatch.Dispatcher;
import io.unifycom.netty.channel.purejavacomm.PureJavaCommChannel;
import io.unifycom.netty.channel.purejavacomm.PureJavaCommChannelConfig;
import io.unifycom.netty.channel.purejavacomm.PureJavaCommDeviceAddress;
import io.unifycom.netty.client.AbstractSocketChannel;
import io.unifycom.netty.client.AbstractSocketChannelInboundHandler;
import io.unifycom.netty.client.AutoReconnectIdleStateHandler;
import io.unifycom.netty.codec.NettyChannelDecoder;
import io.unifycom.netty.codec.NettyChannelEncoder;
import io.unifycom.netty.codec.NettyCodecs;
import io.unifycom.netty.util.EventLoopGroups;

@SuppressWarnings("deprecation")
public class RxtxChannel extends AbstractSocketChannel {

    private static final EventLoopGroup WORKER_GROUP = EventLoopGroups.newOioEventLoopGroup();

    static {
        EventLoopGroups.shutdownHook(WORKER_GROUP);
    }

    public RxtxChannel(RxtxChannelConfig config, NettyChannelDecoder channelDecoder, NettyChannelEncoder<?> channelEncoder,
                       Dispatcher channelDispatcher) {

        super(config, channelDecoder, channelEncoder, channelDispatcher);
    }

    @Override
    protected EventLoopGroup workerGroup() {

        return WORKER_GROUP;
    }

    @Override
    protected ChannelFactory<? extends io.netty.channel.Channel> channelFactory() {

        RxtxChannelConfig config = (RxtxChannelConfig)this.config;

        return (ChannelFactory<PureJavaCommChannel>)() -> {

            PureJavaCommChannel channel = new PureJavaCommChannel();
            channel.config().setBaudrate(config.getBaudrate());
            channel.config().setDatabits(PureJavaCommChannelConfig.Databits.valueOf(config.getDatabits()));
            channel.config().setParitybit(PureJavaCommChannelConfig.Paritybit.valueOf(config.getParitybit()));
            channel.config().setStopbits(PureJavaCommChannelConfig.Stopbits.valueOf(config.getStopbits()));
            return channel;
        };
    }

    @Override
    protected void configureBootstrap(Bootstrap bootstrap) {

        RxtxChannelConfig config = (RxtxChannelConfig)this.config;

        bootstrap.remoteAddress(new PureJavaCommDeviceAddress(config.getPort()));
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {

        RxtxChannelConfig config = (RxtxChannelConfig)this.config;

        pipeline.addLast(new AutoReconnectIdleStateHandler(0, 0, config.getPingIntervalSeconds(), config.getAutoConnectIntervalSeconds(),
                                                           this::connect0));

        NettyCodecs.addByteCodecHandlers(pipeline, channelEncoder, channelDecoder);

        pipeline.addLast(new AbstractSocketChannelInboundHandler(channelDispatcher, this, ping));
    }
}
