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
import io.unifycom.socket.client.AbstractSocketChannel;
import io.unifycom.socket.codec.SocketChannelDecoder;
import io.unifycom.socket.codec.SocketChannelEncoder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class BluetoothChannel extends AbstractSocketChannel {

    private static final Logger logger = LoggerFactory.getLogger(BluetoothChannel.class);

    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private final String id = BluetoothChannel.class.getSimpleName() + "-" + COUNTER.getAndIncrement();

    private static final EventLoopGroup WORKER_GROUP = new OioEventLoopGroup();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(WORKER_GROUP::shutdownGracefully));
    }

    private final SocketChannelDecoder channelDecoder;
    private final SocketChannelEncoder<?> channelEncoder;

    public BluetoothChannel(BluetoothChannelConfig config, SocketChannelDecoder channelDecoder, SocketChannelEncoder<?> channelEncoder,
                            ChannelDispatcher channelDispatcher) {

        super.config = config;

        this.channelDecoder = channelDecoder;

        this.channelEncoder = channelEncoder;
        this.channelDispatcher = channelDispatcher;

        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    @Override
    public synchronized Channel connect() {

        BluetoothChannelConfig config = (BluetoothChannelConfig)this.config;

        if (isActive()) {

            logger.warn("{} is active, don't connect it again.", getName());
            return this;
        }

        lock = new CountDownLatch(1);
        bootstrap = new Bootstrap();

        bootstrap.group(WORKER_GROUP).channel(OioBluetoothChannel.class).remoteAddress(new BluetoothDeviceAddress(config.getConnectionString()))
            .handler(new ChannelInitializer<OioBluetoothChannel>() {

                @Override
                public void initChannel(OioBluetoothChannel ch) {

                    ch.pipeline().addLast(new IdleStateHandler(0, 0, config.getPingIntervalSeconds()) {

                        @Override
                        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

                            ctx.channel().eventLoop()
                                .schedule(BluetoothChannel.super::connect0, config.getAutoConnectIntervalSeconds(), TimeUnit.SECONDS);
                        }
                    }).addLast(channelEncoder.getMessageToByteEncoder()).addLast(channelDecoder.getByteToMessageDecoder());

                    if (channelDecoder.getMessageToMessageDecoder() != null) {

                        ch.pipeline().addLast(channelDecoder.getMessageToMessageDecoder());
                    }

                    ch.pipeline().addLast(new BluetoothChannelInboundHandler(channelDispatcher, BluetoothChannel.this, ping));
                }
            });

        super.connect0();

        logger.info("{} client is connecting to {} ...... ", getName(), config.getConnectionString());

        return this;
    }
}