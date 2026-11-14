package io.unifycom.websocket.server;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.unifycom.Channel;
import io.unifycom.dispatch.Dispatcher;
import io.unifycom.event.ConnectedEvent;
import io.unifycom.event.DisconnectedEvent;
import io.unifycom.event.IdleEvent;
import io.unifycom.netty.server.AbstractNettyChannelRegistry;
import io.unifycom.netty.util.IdleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WebsocketChannelInboundHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketChannelInboundHandler.class);

    private final AbstractNettyChannelRegistry channelHolder;
    private final Dispatcher channelDispatcher;

    public WebsocketChannelInboundHandler(Dispatcher channelDispatcher, AbstractNettyChannelRegistry channelHolder) {

        this.channelHolder = channelHolder;
        this.channelDispatcher = channelDispatcher;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {

            Channel wsClientChannel = new WebsocketSessionChannel(ctx.channel());
            channelHolder.put(wsClientChannel);

            logger.debug("Connection from {} is active.", ctx.channel().remoteAddress());
            ConnectedEvent event = new ConnectedEvent(ctx.channel().id().asShortText(), ctx.channel().remoteAddress().toString());

            channelDispatcher.fire(wsClientChannel, event);

            return;
        }

        if (evt instanceof IdleStateEvent) {

            IdleEvent idleEvent = IdleUtils.toEvent((IdleStateEvent)evt);

            if (idleEvent != null) {
                channelDispatcher.fire(channelHolder.get(ctx.channel()), idleEvent);
            }

            return;
        }

        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        super.channelInactive(ctx);

        Channel wsClientChannel = channelHolder.remove(ctx.channel());

        logger.debug("Connection from {} is inactive.", ctx.channel().remoteAddress());
        DisconnectedEvent event = new DisconnectedEvent(ctx.channel().id().asShortText(), ctx.channel().remoteAddress().toString());

        channelDispatcher.fire(wsClientChannel, event);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        Channel wsClientChannel = channelHolder.get(ctx.channel());

        if (msg instanceof TextWebSocketFrame) {

            channelDispatcher.fire(wsClientChannel, ((TextWebSocketFrame)msg).text());
        } else if (msg instanceof BinaryWebSocketFrame) {

            channelDispatcher.fire(wsClientChannel, ByteBufUtil.getBytes(((BinaryWebSocketFrame)msg).content()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        String info = String.format("%s threw exception.", ctx.channel().remoteAddress());
        logger.error(info, cause);

        ctx.close();
    }
}
