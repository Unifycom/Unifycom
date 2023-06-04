package io.unifycom.netty.server;

import io.unifycom.Channel;
import io.unifycom.dispatch.ChannelDispatcher;
import io.unifycom.event.ConnectedEvent;
import io.unifycom.event.DisconnectedEvent;
import io.unifycom.event.IdleEvent;
import io.unifycom.netty.util.IdleUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractNettyChannelInboundHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractNettyChannelInboundHandler.class);

    private AbstractNettyChannelGroup nettyChannelGroup;
    private ChannelDispatcher channelDispatcher;

    public AbstractNettyChannelInboundHandler(ChannelDispatcher channelDispatcher, AbstractNettyChannelGroup nettyChannelGroup) {

        this.nettyChannelGroup = nettyChannelGroup;
        this.channelDispatcher = channelDispatcher;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        super.channelActive(ctx);

        Channel channel = new NettyChannel(ctx.channel());
        nettyChannelGroup.put(channel);

        logger.debug("Connection from {} is active.", ctx.channel().remoteAddress());
        ConnectedEvent event = new ConnectedEvent(ctx.channel().id().asShortText(), ctx.channel().remoteAddress().toString());

        channelDispatcher.fire(channel, event);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        super.channelInactive(ctx);
        Channel channel = nettyChannelGroup.remove(ctx.channel());

        logger.debug("Connection from {} is inactive.", ctx.channel().remoteAddress());
        DisconnectedEvent event = new DisconnectedEvent(ctx.channel().id().asShortText(), ctx.channel().remoteAddress().toString());

        channelDispatcher.fire(channel, event);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        try {
            super.exceptionCaught(ctx, cause);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        String info = String.format("%s threw exception.", ctx.channel().remoteAddress());
        logger.error(info, cause);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object in) throws Exception {

        if (channelDispatcher != null) {

            channelDispatcher.fire(nettyChannelGroup.get(ctx.channel()), in);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {

            IdleEvent idleEvent = IdleUtils.toEvent((IdleStateEvent)evt);

            if (idleEvent != null) {

                channelDispatcher.fire(nettyChannelGroup.get(ctx.channel()), idleEvent);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
