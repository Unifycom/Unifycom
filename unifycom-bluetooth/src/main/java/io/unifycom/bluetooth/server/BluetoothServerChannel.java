package io.unifycom.bluetooth.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.handler.timeout.IdleStateHandler;
import io.unifycom.AbstractServerChannel;
import io.unifycom.Channel;
import io.unifycom.codec.AbstractChannelDecoder;
import io.unifycom.dispatch.Dispatcher;
import io.unifycom.netty.codec.NettyChannelDecoder;
import io.unifycom.netty.codec.NettyChannelEncoder;
import java.io.IOException;
import java.util.concurrent.Future;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class BluetoothServerChannel extends AbstractServerChannel {

    private static final Logger logger = LoggerFactory.getLogger(BluetoothServerChannel.class);

    private BluetoothServerChannelConfig config;
    private NettyChannelDecoder channelDecoder;
    private NettyChannelEncoder<?> channelEncoder;

    private EventLoopGroup workerGroup = new OioEventLoopGroup();
    private BluetoothChannelRegistry channelHolder = new BluetoothChannelRegistry();

    private StreamConnectionNotifier notifier;
    private Thread acceptorThread;
    private volatile boolean ready = false;

    public BluetoothServerChannel(BluetoothServerChannelConfig config, NettyChannelDecoder channelDecoder, NettyChannelEncoder<?> channelEncoder,
                                  Dispatcher channelDispatcher) {

        this.config = config;

        this.channelDecoder = channelDecoder;
        ((AbstractChannelDecoder)this.channelDecoder).setMaxInboundMessageSize(this.config.getMaxInboundMessageSize());

        this.channelEncoder = channelEncoder;
        this.channelDispatcher = channelDispatcher;

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
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

    private void initChannel(OioBluetoothAcceptedChannel ch) {

        ch.pipeline().addLast(
                new IdleStateHandler(config.getReaderIdleTimeSeconds(), config.getWriterIdleTimeSeconds(), config.getAllIdleTimeSeconds()))
            .addLast(channelEncoder.getMessageToByteEncoder()).addLast(channelDecoder.getByteToMessageDecoder());

        if (channelDecoder.getMessageToMessageDecoder() != null) {

            ch.pipeline().addLast(channelDecoder.getMessageToMessageDecoder());
        }

        ch.pipeline().addLast(new BluetoothChannelInboundHandler(channelDispatcher, channelHolder));
    }

    @Override
    public boolean isReady() {

        return ready && acceptorThread != null && acceptorThread.isAlive();
    }

    @Override
    public Future<Void> send(String channelName, Object out) throws IOException {

        Channel ch = getChannel(channelName);

        if (ch == null) {

            logger.error("Not found any channel by name {}.", channelName);
            return null;
        }

        return ch.send(out);
    }

    @Override
    public Channel getChannel(String channelName) {

        return channelHolder.getByName(channelName);
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

        channelHolder.all().forEach(Channel::close);

        if (workerGroup != null) {

            workerGroup.shutdownGracefully();
            logger.debug("WorkerGroup of {} on {} has been shutdown.", getName(), config.getConnectionString());
        }

        if (channelDispatcher != null) {

            channelDispatcher.close();
            logger.info("Dispatcher of {} on {} has been shutdown gracefully.", getName(), config.getConnectionString());
        }

        notifier = null;
        acceptorThread = null;

        logger.info("{} on {} has been shutdown gracefully.", getName(), config.getConnectionString());
    }
}
