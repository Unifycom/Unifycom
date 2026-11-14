package io.unifycom.websocket.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.unifycom.dispatch.Dispatcher;
import io.unifycom.event.ConnectedEvent;
import io.unifycom.event.DisconnectedEvent;
import io.unifycom.event.IdleEvent;
import io.unifycom.netty.util.IdleUtils;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WebsocketChannelInboundHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketChannelInboundHandler.class);

    private final WebsocketChannel channel;
    private final Dispatcher channelDispatcher;

    public WebsocketChannelInboundHandler(Dispatcher channelDispatcher, WebsocketChannel channel) {

        this.channel = channel;
        this.channelDispatcher = channelDispatcher;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {

            channel.onHandshakeComplete();

            logger.debug("Connection to {} is active.", ctx.channel().remoteAddress());
            ConnectedEvent event = new ConnectedEvent(ctx.channel().id().asShortText(), ctx.channel().remoteAddress().toString());

            channelDispatcher.fire(channel, event);

            return;
        }

        if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_TIMEOUT) {

            logger.warn("Handshake of {} has been timeout.", ctx.channel().remoteAddress());

            return;
        }

        if (evt instanceof IdleStateEvent) {

            IdleEvent idleEvent = IdleUtils.toEvent((IdleStateEvent)evt);

            if (idleEvent != null) {

                channelDispatcher.fire(channel, idleEvent);
            }

            ctx.writeAndFlush(new PingWebSocketFrame()).addListener(future -> {

                if (!future.isSuccess()) {

                    logger.warn("Ping of {} failed.", ctx.channel().remoteAddress(), future.cause());
                }
            });

            return;
        }

        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        super.channelInactive(ctx);

        logger.debug("Connection to {} is inactive.", ctx.channel().remoteAddress());
        DisconnectedEvent event = new DisconnectedEvent(ctx.channel().id().asShortText(), ctx.channel().remoteAddress().toString());

        channelDispatcher.fire(channel, event);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof TextWebSocketFrame) {

            channelDispatcher.fire(channel, ((TextWebSocketFrame)msg).text());
        } else if (msg instanceof BinaryWebSocketFrame) {

            channelDispatcher.fire(channel, ((BinaryWebSocketFrame)msg).content().toString(StandardCharsets.UTF_8));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        String info = String.format("%s threw exception.", ctx.channel().remoteAddress());
        logger.error(info, cause);

        ctx.close();
    }
}
