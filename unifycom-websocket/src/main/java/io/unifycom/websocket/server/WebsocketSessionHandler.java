package io.unifycom.websocket.server;

import io.undertow.websockets.core.*;
import io.unifycom.Channel;
import io.unifycom.dispatch.ChannelDispatcher;
import io.unifycom.event.ConnectedEvent;
import io.unifycom.event.DisconnectedEvent;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WebsocketSessionHandler implements WebSocketConnectionCallback {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketSessionHandler.class);

    private WebsocketChannelHolder channelHolder;
    private ChannelDispatcher channelDispatcher;

    public WebsocketSessionHandler(ChannelDispatcher channelDispatcher, WebsocketChannelHolder channelHolder) {

        this.channelHolder = channelHolder;
        this.channelDispatcher = channelDispatcher;
    }

    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel wsChannel) {

        Channel wsClientChannel = new WebsocketSessionChannel(wsChannel);
        channelHolder.put(wsClientChannel);

        ConnectedEvent event = new ConnectedEvent(wsChannel.toString(), wsChannel.getSourceAddress().toString());
        logger.debug("Connection from {} is active.", event.getRemoteAddress());

        channelDispatcher.fire(wsClientChannel, event);

        wsChannel.getReceiveSetter().set(new AbstractReceiveListener() {

            @Override
            protected void onFullTextMessage(WebSocketChannel wsChannel, BufferedTextMessage message) {

                channelDispatcher.fire(wsClientChannel, message.getData());
            }

            @Override
            protected void onFullBinaryMessage(WebSocketChannel wsChannel, BufferedBinaryMessage message) {

                channelDispatcher.fire(wsClientChannel, message.getData());
            }

            @Override
            protected void onCloseMessage(CloseMessage closeMessage, WebSocketChannel wsChannel) {

                Channel wsClientChannel = channelHolder.remove(wsChannel);

                DisconnectedEvent event = new DisconnectedEvent(wsChannel.toString(), wsChannel.getSourceAddress().toString());
                logger.debug("Connection from {} is inactive.", event.getRemoteAddress());

                channelDispatcher.fire(wsClientChannel, event);
            }

            @Override
            protected void onError(WebSocketChannel wsChannel, Throwable error) {

                super.onError(wsChannel, error);

                DisconnectedEvent event = new DisconnectedEvent(wsChannel.toString(), wsChannel.getSourceAddress().toString());
                logger.debug("Connection from {} is inactive.", event.getRemoteAddress());

                channelDispatcher.fire(wsClientChannel, event);
            }
        });

        wsChannel.resumeReceives();
    }
}
