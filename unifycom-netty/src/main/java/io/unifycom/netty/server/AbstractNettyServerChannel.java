package io.unifycom.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.unifycom.AbstractServerChannel;
import io.unifycom.Channel;
import io.unifycom.codec.AbstractChannelDecoder;
import io.unifycom.netty.AbstractServerHostPortChannelConfig;
import io.unifycom.netty.AbstractSocketChannelConfig;
import io.unifycom.netty.codec.NettyChannelDecoder;
import io.unifycom.netty.codec.NettyChannelEncoder;
import io.unifycom.netty.util.EventLoopGroups;
import java.io.IOException;
import java.util.concurrent.Future;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The skeleton of the unifycom server channels based on netty {@link ServerBootstrap}, it provides the template of
 * {@link #startup()} and the common {@link #shutdown()}/{@link #isReady()}/{@link #send(String, Object)}/
 * {@link #getChannel(String)} implementations. The subclasses only need to configure the bootstrap
 * ({@link #configureServerBootstrap(ServerBootstrap)}) and assemble the pipeline of the accepted channels
 * ({@link #initChannel(io.netty.channel.Channel)}).
 * <p>
 * The transports which are not based on {@link ServerBootstrap} (udp datagram, bluetooth oio acceptor) override
 * {@link #startup()} with their own binding/accepting flow, but still reuse the rest of this skeleton.
 */
public abstract class AbstractNettyServerChannel extends AbstractServerChannel {

    private static final Logger logger = LoggerFactory.getLogger(AbstractNettyServerChannel.class);

    protected AbstractSocketChannelConfig config;
    protected NettyChannelDecoder channelDecoder;
    protected NettyChannelEncoder<?> channelEncoder;
    protected AbstractNettyChannelRegistry channelHolder;

    protected ServerBootstrap serverBootstrap;
    protected EventLoopGroup bossGroup;
    protected EventLoopGroup workerGroup;

    protected AbstractNettyServerChannel(AbstractSocketChannelConfig config, NettyChannelDecoder channelDecoder,
                                         NettyChannelEncoder<?> channelEncoder, AbstractNettyChannelRegistry channelHolder) {

        this.config = config;
        this.channelDecoder = channelDecoder;

        if (channelDecoder != null) {
            ((AbstractChannelDecoder)channelDecoder).setMaxInboundMessageSize(config.getMaxInboundMessageSize());
        }

        this.channelEncoder = channelEncoder;
        this.channelHolder = channelHolder;

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    /**
     * The template method to start up the server based on {@link ServerBootstrap}.
     */
    @Override
    public synchronized AbstractServerChannel startup() {

        if (serverBootstrap != null) {

            logger.warn("{} is listening on {}, don't start-up it again.", getName(), config.getConnectionString());
            return this;
        }

        serverBootstrap = new ServerBootstrap();
        bossGroup = EventLoopGroups.newDefaultEventLoopGroup(1);
        workerGroup = EventLoopGroups.newDefaultEventLoopGroup();

        serverBootstrap.group(bossGroup, workerGroup).channel(serverChannelClass())
            .childHandler(new ChannelInitializer<io.netty.channel.Channel>() {

                @Override
                protected void initChannel(io.netty.channel.Channel ch) throws Exception {
                    AbstractNettyServerChannel.this.initChannel(ch);
                }
            });

        configureServerBootstrap(serverBootstrap);

        bind(serverBootstrap);

        logger.info("{} server is listening on {} ...... ", getName(), config.getConnectionString());

        return this;
    }

    /**
     * The server socket channel class matching the {@link #workerGroup}.
     */
    protected Class<? extends ServerSocketChannel> serverChannelClass() {

        return (workerGroup instanceof EpollEventLoopGroup) ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }

    /**
     * Configures the child options of the server bootstrap.
     */
    protected void configureServerBootstrap(ServerBootstrap serverBootstrap) {

        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
    }

    /**
     * Assembles the pipeline of the accepted channels, including the idle state handler, the codecs and the
     * inbound handler.
     */
    protected abstract void initChannel(io.netty.channel.Channel ch);

    /**
     * Binds the server bootstrap to the configured host/port, it requires the config to be a
     * {@link AbstractServerHostPortChannelConfig}, the transports with another address scheme override
     * {@link #startup()} and never invoke it.
     */
    protected void bind(ServerBootstrap serverBootstrap) {

        AbstractServerHostPortChannelConfig config = (AbstractServerHostPortChannelConfig)this.config;

        if (StringUtils.isEmpty(config.getHost())) {
            serverBootstrap.bind(config.getPort());
        } else {
            serverBootstrap.bind(config.getHost(), config.getPort());
        }
    }

    /**
     * Adds the byte stream codecs (MessageToByteEncoder + ByteToMessageDecoder + optional MessageToMessageDecoder)
     * of the unifycom codec SPI to the pipeline.
     */
    protected void addCodecHandlers(ChannelPipeline pipeline) {

        io.unifycom.netty.codec.NettyCodecs.addByteCodecHandlers(pipeline, channelEncoder, channelDecoder);
    }

    @Override
    public boolean isReady() {

        return workerGroup != null && !workerGroup.isShutdown() && !workerGroup.isTerminated() && !workerGroup.isShuttingDown();
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

    /**
     * Invoked before the sessions are closed and the groups are shut down, the transports with extra resources
     * (e.g. the udp server channel) override it to release their own resources.
     */
    protected void beforeShutdown() {

    }

    @Override
    public void shutdown() {

        beforeShutdown();

        if (channelHolder != null) {

            channelHolder.all().forEach(Channel::close);
        }

        if (workerGroup != null) {

            workerGroup.shutdownGracefully();
            logger.debug("WorkerGroup of {} on {} has been shutdown.", getName(), config.getConnectionString());
        }

        if (bossGroup != null) {

            bossGroup.shutdownGracefully();
            logger.debug("BossGroup of {} on {} has been shutdown.", getName(), config.getConnectionString());
        }

        if (channelDispatcher != null) {

            channelDispatcher.close();
            logger.info("Dispatcher of {} on {} has been shutdown gracefully.", getName(), config.getConnectionString());
        }

        serverBootstrap = null;
        logger.info("{} on {} has been shutdown gracefully.", getName(), config.getConnectionString());
    }
}
