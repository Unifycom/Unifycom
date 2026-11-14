package io.unifycom.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.unifycom.AbstractChannel;
import io.unifycom.AbstractChannelConfig;
import io.unifycom.Channel;
import io.unifycom.Ping;
import io.unifycom.codec.AbstractChannelDecoder;
import io.unifycom.dispatch.Dispatcher;
import io.unifycom.netty.AbstractSocketChannelConfig;
import io.unifycom.netty.codec.NettyChannelDecoder;
import io.unifycom.netty.codec.NettyChannelEncoder;
import io.unifycom.netty.util.EventLoopGroups;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The skeleton of all unifycom client channels based on netty, it provides the template of {@link #connect()},
 * the subclasses only need to configure the bootstrap ({@link #workerGroup()}/{@link #channelClass()}/
 * {@link #channelFactory()}/{@link #configureBootstrap(Bootstrap)}) and assemble their own pipeline
 * ({@link #initPipeline(ChannelPipeline)}).
 */
public abstract class AbstractSocketChannel extends AbstractChannel {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSocketChannel.class);

    /**
     * The shared epoll/nio worker group, it is created lazily on first use so that the oio transports
     * (bluetooth/rxtx) never pay for it, and it is shut down by a shutdown hook.
     */
    private static final class DefaultWorkerGroupHolder {

        private static final EventLoopGroup GROUP = EventLoopGroups.newDefaultEventLoopGroup();

        static {
            EventLoopGroups.shutdownHook(GROUP);
        }
    }

    protected Ping ping;
    protected CountDownLatch lock;

    protected AbstractChannelConfig config;

    protected NettyChannelDecoder channelDecoder;
    protected NettyChannelEncoder<?> channelEncoder;

    protected Bootstrap bootstrap;
    protected io.netty.channel.Channel channel;

    protected AbstractSocketChannel(AbstractSocketChannelConfig config, NettyChannelDecoder channelDecoder,
                                    NettyChannelEncoder<?> channelEncoder, Dispatcher channelDispatcher) {

        this.config = config;
        this.channelDecoder = channelDecoder;

        if (channelDecoder != null) {
            ((AbstractChannelDecoder)channelDecoder).setMaxInboundMessageSize(config.getMaxInboundMessageSize());
        }

        this.channelEncoder = channelEncoder;
        this.channelDispatcher = channelDispatcher;

        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    /**
     * The template method to connect the channel to its remote peer.
     */
    @Override
    public synchronized Channel connect() {

        if (isActive()) {
            logger.warn("{} is active, don't connect it again.", getName());
            return this;
        }

        beforeConnect();

        lock = new CountDownLatch(1);
        bootstrap = new Bootstrap();

        initBootstrap(bootstrap);

        configureBootstrap(bootstrap);

        doConnect();

        logger.info("{} client is connecting to {} ...... ", getName(), config.getConnectionString());

        return this;
    }

    private void initBootstrap(Bootstrap bootstrap) {

        bootstrap.group(workerGroup());

        if (channelFactory() != null) {
            bootstrap.channelFactory(channelFactory());
        } else {
            bootstrap.channel(channelClass());
        }

        bootstrap.handler(new ChannelInitializer<io.netty.channel.Channel>() {

            @Override
            public void initChannel(io.netty.channel.Channel ch) {
                initPipeline(ch.pipeline());
            }
        });
    }

    /**
     * Invoked before the channel starts connecting, the subclasses can reset their own state (e.g. the
     * websocket handshaked flag).
     */
    protected void beforeConnect() {

    }

    /**
     * The event loop group used by the client, defaults to the shared epoll/nio group, the oio transports
     * (bluetooth/rxtx) override it with their own oio group.
     */
    protected EventLoopGroup workerGroup() {

        return DefaultWorkerGroupHolder.GROUP;
    }

    /**
     * The netty channel class, defaults to the epoll/nio socket channel matching the {@link #workerGroup()},
     * the datagram transport (udp) overrides it with the datagram channel.
     */
    protected Class<? extends io.netty.channel.Channel> channelClass() {

        return (workerGroup() instanceof EpollEventLoopGroup) ? EpollSocketChannel.class : NioSocketChannel.class;
    }

    /**
     * The netty channel factory, when it is not null it takes precedence over {@link #channelClass()},
     * it is used by the transports whose channels need to be created with a customized configuration (rxtx).
     */
    @SuppressWarnings("deprecation")
    protected ChannelFactory<? extends io.netty.channel.Channel> channelFactory() {

        return null;
    }

    /**
     * Configures the channel options and the remote address of the bootstrap.
     */
    protected abstract void configureBootstrap(Bootstrap bootstrap);

    /**
     * Assembles the pipeline of the channel, including the idle state handler, the codecs and the inbound handler.
     */
    protected abstract void initPipeline(ChannelPipeline pipeline);

    /**
     * Connects or binds the channel, defaults to {@link #connect0()}, the datagram transport (udp) overrides it to bind.
     */
    protected void doConnect() {

        connect0();
    }

    @Override
    public synchronized void close() {

        if (channel != null) {

            channel.close().awaitUninterruptibly(100);
        }

        if (channelDispatcher != null) {

            channelDispatcher.close();
        }

        if (lock != null) {
            lock.countDown();
        }

        channel = null;
        bootstrap = null;
        logger.info("{}{} has been closed.", getName(), StringUtils.isNotBlank(getName()) ? ("[" + getName() + "]") : StringUtils.EMPTY);
    }

    @Override
    public boolean isClosed() {

        return bootstrap == null;
    }

    protected synchronized void connect0() {

        if (isActive() || isClosed()) {

            return;
        }

        bootstrap.connect().addListener((ChannelFutureListener)futureListener -> {

            if (futureListener.isSuccess()) {

                lock.countDown();

                if (channel != null) {
                    channel.close();
                }

                channel = futureListener.channel();
            } else {
                logger.warn("{} is not active, try again after {}s. {}.", getName(), config.getAutoConnectIntervalSeconds(),
                            futureListener.cause().getMessage());
            }
        });
    }

    @Override
    public boolean isActive() {

        return bootstrap != null && channel != null && channel.isActive();
    }

    @Override
    public Future<Void> send(Object out) throws IOException {

        if (!isActive()) {

            throw new IOException("The channel is not ready yet");
        }

        if (!channel.isWritable()) {

            throw new IOException("The channel is not writable");
        }

        return channel.writeAndFlush(out);
    }

    @Override
    public Channel blockUntilConnected() throws InterruptedException {

        lock.await();

        return this;
    }

    @Override
    public Channel blockUntilConnected(int timeout, TimeUnit unit) throws InterruptedException {

        lock.await(timeout, unit);

        return this;
    }

    public void setPing(Ping ping) {

        this.ping = ping;
    }
}
