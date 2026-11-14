package io.unifycom.bluetooth.server;

import io.netty.channel.ChannelFuture;
import io.netty.handler.timeout.IdleStateHandler;
import io.unifycom.AbstractServerChannel;
import io.unifycom.dispatch.Dispatcher;
import io.unifycom.netty.codec.NettyChannelDecoder;
import io.unifycom.netty.codec.NettyChannelEncoder;
import io.unifycom.netty.codec.NettyCodecs;
import io.unifycom.netty.server.AbstractNettyChannelRegistry;
import io.unifycom.netty.server.AbstractNettyServerChannel;
import io.unifycom.netty.server.AbstractSocketChannelInboundHandler;
import io.unifycom.netty.util.EventLoopGroups;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The bluetooth server channel, the bluetooth transport is oio so it accepts the connections by itself with an
 * acceptor thread instead of a {@link io.netty.bootstrap.ServerBootstrap}, it overrides {@link #startup()} with
 * its own accepting flow but still reuses the rest of the server skeleton.
 */
@SuppressWarnings("deprecation")
public class BluetoothServerChannel extends AbstractNettyServerChannel {

    private static final Logger logger = LoggerFactory.getLogger(BluetoothServerChannel.class);

    private StreamConnectionNotifier notifier;
    private Thread acceptorThread;
    private volatile boolean ready = false;

    public BluetoothServerChannel(BluetoothServerChannelConfig config, NettyChannelDecoder channelDecoder, NettyChannelEncoder<?> channelEncoder,
                                  Dispatcher channelDispatcher) {

        super(config, channelDecoder, channelEncoder, new AbstractNettyChannelRegistry());

        this.channelDispatcher = channelDispatcher;
        this.workerGroup = EventLoopGroups.newOioEventLoopGroup();
    }

    @Override
    public synchronized AbstractServerChannel startup() {

        if (notifier != null) {

            logger.warn("{} is listening on {}, don't start-up it again.", getName(), config.getConnectionString());
            return this;
        }

        try {
            notifier = (StreamConnectionNotifier)Connector.open(config.getConnectionString());
        } catch (IOException e) {

            throw new RuntimeException(String.format("Failed to open notifier on %s.", config.getConnectionString()), e);
        }

        ready = true;
        acceptorThread = new Thread(this::acceptLoop, "unifycom-bluetooth-acceptor");
        acceptorThread.setDaemon(true);
        acceptorThread.start();

        logger.info("{} server is listening on {} ...... ", getName(), config.getConnectionString());

        return this;
    }

    private void acceptLoop() {

        while (ready) {

            try {
                StreamConnection connection = notifier.acceptAndOpen();

                OioBluetoothAcceptedChannel ch = new OioBluetoothAcceptedChannel(connection);
                ch.activate();

                initChannel(ch);

                workerGroup.register(ch).addListener((ChannelFuture f) -> {

                    if (!f.isSuccess()) {
                        logger.error("Failed to register channel {}.", ch.remoteAddress(), f.cause());
                        ch.close();
                    }
                });

                logger.info("Connection from {} has been accepted.", ch.remoteAddress());
            } catch (IOException e) {

                if (ready) {
                    logger.error("Failed to accept connection.", e);
                }

                break;
            }
        }
    }

    @Override
    protected void initChannel(io.netty.channel.Channel ch) {

        BluetoothServerChannelConfig config = (BluetoothServerChannelConfig)this.config;

        ch.pipeline().addLast(
                new IdleStateHandler(config.getReaderIdleTimeSeconds(), config.getWriterIdleTimeSeconds(), config.getAllIdleTimeSeconds()));

        NettyCodecs.addByteCodecHandlers(ch.pipeline(), channelEncoder, channelDecoder);

        ch.pipeline().addLast(new AbstractSocketChannelInboundHandler(channelDispatcher, channelHolder));
    }

    @Override
    public boolean isReady() {

        return ready && acceptorThread != null && acceptorThread.isAlive();
    }

    @Override
    public synchronized void shutdown() {

        ready = false;

        if (notifier != null) {

            try {
                notifier.close();
            } catch (IOException e) {
                logger.warn("Failed to close notifier of {} on {}.", getName(), config.getConnectionString(), e);
            }
        }

        super.shutdown();

        notifier = null;
        acceptorThread = null;
    }
}
