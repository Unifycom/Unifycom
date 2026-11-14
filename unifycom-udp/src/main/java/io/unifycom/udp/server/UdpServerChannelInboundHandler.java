package io.unifycom.udp.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.unifycom.Channel;
import io.unifycom.Envelope;
import io.unifycom.dispatch.Dispatcher;
import io.unifycom.event.ConnectedEvent;
import io.unifycom.event.IdleEvent;
import io.unifycom.netty.util.IdleUtils;
import java.net.SocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpServerChannelInboundHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = LoggerFactory.getLogger(UdpServerChannelInboundHandler.class);

    private final UdpChannelRegistry channelHolder;
    private final Dispatcher channelDispatcher;

    public UdpServerChannelInboundHandler(Dispatcher channelDispatcher, UdpChannelRegistry channelHolder) {

        this.channelHolder = channelHolder;
        this.channelDispatcher = channelDispatcher;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        super.channelActive(ctx);
        logger.info("Connection of {} is active.", ctx.channel().localAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        super.channelInactive(ctx);
        logger.info("Connection of {} is inactive.", ctx.channel().localAddress());
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

        if (channelDispatcher == null) {
            return;
        }

        if (!(in instanceof Envelope)) {

            logger.warn("Unsupported message {}, ignored.", in);
            return;
        }

        Envelope envelope = (Envelope)in;
        SocketAddress sender = (SocketAddress)envelope.getSender();

        Channel channel = channelHolder.get(sender);

        if (channel == null) {

            channel = new UdpSessionChannel(ctx.channel(), sender);
            channelHolder.put(channel);

            logger.debug("Connection from {} is active.", sender);
            channelDispatcher.fire(channel, new ConnectedEvent(ctx.channel().id().asShortText(), sender.toString()));
        }

        channelDispatcher.fire(channel, envelope);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {

            IdleEvent idleEvent = IdleUtils.toEvent((IdleStateEvent)evt);

            if (idleEvent != null && channelDispatcher != null) {

                channelHolder.all().forEach(ch -> channelDispatcher.fire(ch, idleEvent));
            }

        } else {

            super.userEventTriggered(ctx, evt);
        }
    }
}
