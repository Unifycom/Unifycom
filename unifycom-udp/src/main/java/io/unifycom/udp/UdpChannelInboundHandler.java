package io.unifycom.udp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.unifycom.Channel;
import io.unifycom.Ping;
import io.unifycom.dispatch.ChannelDispatcher;
import io.unifycom.event.ConnectedEvent;
import io.unifycom.event.DisconnectedEvent;
import io.unifycom.event.IdleEvent;
import io.unifycom.netty.codec.DefaultNettyChannelDecoder;
import io.unifycom.netty.util.IdleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpChannelInboundHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = LoggerFactory.getLogger(UdpChannelInboundHandler.class);

    private Channel channel;
    private final Ping ping;
    private ChannelDispatcher channelDispatcher;
    private DefaultNettyChannelDecoder channelDecoder;

    public UdpChannelInboundHandler(ChannelDispatcher channelEventDispatcher) {

        this(channelEventDispatcher, null, null);
    }

    public UdpChannelInboundHandler(ChannelDispatcher channelDispatcher, Channel channel, Ping ping) {

        this.ping = ping;
        this.channel = channel;
        this.channelDispatcher = channelDispatcher;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        super.channelActive(ctx);

        logger.info("Connection of {} is active.", ctx.channel().localAddress());
        ConnectedEvent event = new ConnectedEvent(ctx.channel().id().asShortText(), ctx.channel().localAddress().toString());

        channelDispatcher.fire(channel, event);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        super.channelInactive(ctx);

        logger.info("Connection of {} is inactive.", ctx.channel());
        DisconnectedEvent event = new DisconnectedEvent(ctx.channel().id().asShortText(), ctx.channel().localAddress().toString());

        channelDispatcher.fire(channel, event);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        try {
            super.exceptionCaught(ctx, cause);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        String info = String.format("%s threw exception.", ctx.channel().localAddress());
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
