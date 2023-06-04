package io.unifycom.websocket.client;

import io.unifycom.dispatch.ChannelDispatcher;
import io.unifycom.event.ConnectedEvent;
import io.unifycom.event.DisconnectedEvent;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.IoFuture.HandlingNotifier;

class WebsocketHandlingNotifier extends HandlingNotifier<WebSocketChannel, URI> {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketHandlingNotifier.class);

    private WebsocketChannel channel;
    private ChannelDispatcher channelDispatcher;

    public WebsocketHandlingNotifier(ChannelDispatcher channelDispatcher, WebsocketChannel channel) {

        this.channel = channel;
        this.channelDispatcher = channelDispatcher;
    }

    @Override
    public void handleCancelled(final URI uri) {

        logger.warn("Connection of {} is cancelled.", uri);
    }

    @Override
    public void handleDone(final WebSocketChannel wsChannel, final URI uri) {

        channel.wsChannel = wsChannel;

        ConnectedEvent event = new ConnectedEvent(wsChannel.toString(), wsChannel.getDestinationAddress().toString());
        logger.debug("Connection to {} is active.", event.getRemoteAddress());

        channelDispatcher.fire(channel, event);

        wsChannel.getReceiveSetter().set(new AbstractReceiveListener() {

            @Override
            protected void onFullTextMessage(WebSocketChannel wsChannel, BufferedTextMessage message) {

                channelDispatcher.fire(channel, message.getData());
            }

            @Override
            protected void onCloseMessage(CloseMessage closeMessage, WebSocketChannel wsChannel) {

                DisconnectedEvent event = new DisconnectedEvent(wsChannel.toString(), wsChannel.getDestinationAddress().toString());
                logger.debug("Connection to {} is inactive.", event.getRemoteAddress());

                channelDispatcher.fire(channel, event);
            }

            @Override
            protected void onError(WebSocketChannel wsChannel, Throwable error) {

                super.onError(wsChannel, error);

                DisconnectedEvent event = new DisconnectedEvent(wsChannel.toString(), wsChannel.getDestinationAddress().toString());
                logger.debug("Connection of {} is inactive.", event.getRemoteAddress());

                channelDispatcher.fire(channel, event);
            }
        });

        wsChannel.resumeReceives();
    }
}
