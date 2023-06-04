package io.unifycom.bluetooth.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.handler.timeout.IdleStateHandler;
import io.unifycom.Channel;
import io.unifycom.dispatch.ChannelDispatcher;
import io.unifycom.netty.channel.bluetooth.BluetoothDeviceAddress;
import io.unifycom.netty.channel.bluetooth.OioBluetoothChannel;
import io.unifycom.netty.client.AbstractNettyChannel;
import io.unifycom.netty.codec.NettyChannelDecoder;
import io.unifycom.netty.codec.NettyChannelEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("deprecation")
public class BluetoothChannel extends AbstractNettyChannel {

    private static final Logger logger = LoggerFactory.getLogger(BluetoothChannel.class);

    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private final String id = BluetoothChannel.class.getSimpleName() + "-" + COUNTER.getAndIncrement();

    private static final EventLoopGroup WORKER_GROUP = new OioEventLoopGroup();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(WORKER_GROUP::shutdownGracefully));
    }

    private final NettyChannelDecoder channelDecoder;
    private final NettyChannelEncoder<?> channelEncoder;

    public BluetoothChannel(BluetoothChannelConfig config, NettyChannelDecoder channelDecoder, NettyChannelEncoder<?> channelEncoder, ChannelDispatcher channelDispatcher) {

        super.config = config;

        this.channelDecoder = channelDecoder;

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

        BluetoothChannelConfig config = (BluetoothChannelConfig) this.config;

        if (isActive()) {

            logger.warn("{} is active, don't connect it again.", getId());
            return this;
        }

        lock = new CountDownLatch(1);
        bootstrap = new Bootstrap();

        bootstrap.group(WORKER_GROUP).channel(OioBluetoothChannel.class).remoteAddress(
                new BluetoothDeviceAddress(config.getConnectionString())).handler(new ChannelInitializer<OioBluetoothChannel>() {

            @Override
            public void initChannel(OioBluetoothChannel ch) {

                ch.pipeline().addLast(new IdleStateHandler(0, 0, config.getPingIntervalSeconds()) {

                    @Override
                    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

                        ctx.channel().eventLoop().schedule(BluetoothChannel.super::connect0, config.getAutoConnectIntervalSeconds(), TimeUnit.SECONDS);
                    }
                }).addLast(channelEncoder.getMessageToByteEncoder()).addLast(channelDecoder.getByteToMessageDecoder());

                if (channelDecoder.getMessageToMessageDecoder() != null) {

                    ch.pipeline().addLast(channelDecoder.getMessageToMessageDecoder());
                }

                ch.pipeline().addLast(new BluetoothChannelInboundHandler(channelDispatcher, BluetoothChannel.this, ping));
            }
        });

        super.connect0();

        logger.info("{} client is connecting to {} ...... ", getId(), config.getConnectionString());

        return this;
    }
}