package io.unifycom.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.unifycom.Channel;
import io.unifycom.Ping;
import io.unifycom.dispatch.Dispatcher;
import io.unifycom.event.ConnectedEvent;
import io.unifycom.event.DisconnectedEvent;
import io.unifycom.event.IdleEvent;
import io.unifycom.netty.util.IdleUtils;
import java.net.SocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The inbound handler of all unifycom client channels, it fires the connected/disconnected/idle events and
 * the decoded messages to the dispatcher.
 */
public class AbstractSocketChannelInboundHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSocketChannelInboundHandler.class);

    private Channel channel;
    private final Ping ping;
    private Dispatcher channelDispatcher;

    public AbstractSocketChannelInboundHandler(Dispatcher channelEventDispatcher) {

        this(channelEventDispatcher, null, null);
    }

    public AbstractSocketChannelInboundHandler(Dispatcher channelDispatcher, Channel channel, Ping ping) {

        this.ping = ping;
        this.channel = channel;
        this.channelDispatcher = channelDispatcher;
    }

    /**
     * The peer address used in the logs and the connected/disconnected events, defaults to the remote address,
     * the datagram transport (udp) overrides it with the local address because its channel is bound locally.
     */
    protected SocketAddress peerAddress(ChannelHandlerContext ctx) {

        return ctx.channel().remoteAddress();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        super.channelActive(ctx);

        logger.info("Connection of {} is active.", peerAddress(ctx));
        ConnectedEvent event = new ConnectedEvent(ctx.channel().id().asShortText(), peerAddress(ctx).toString());

        channelDispatcher.fire(channel, event);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        super.channelInactive(ctx);

        logger.info("Connection of {} is inactive.", peerAddress(ctx));
        DisconnectedEvent event = new DisconnectedEvent(ctx.channel().id().asShortText(), peerAddress(ctx).toString());

        channelDispatcher.fire(channel, event);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        try {
            super.exceptionCaught(ctx, cause);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        String info = String.format("%s threw exception.", peerAddress(ctx));
        logger.error(info, cause);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object in) throws Exception {

        if (channelDispatcher != null) {

            channelDispatcher.fire(channel, in);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {

            IdleEvent idleEvent = IdleUtils.toEvent((IdleStateEvent)evt);

            if (idleEvent != null) {

                channelDispatcher.fire(channel, idleEvent);
            }

            if (ping != null) {

                ctx.writeAndFlush(ping.ping()).get();
            }
        } else {

            super.userEventTriggered(ctx, evt);
        }
    }
}
