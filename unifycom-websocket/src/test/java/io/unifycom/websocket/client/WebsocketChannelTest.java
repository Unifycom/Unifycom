package io.unifycom.websocket.client;

import io.unifycom.event.ChannelEvent;
import io.unifycom.event.ConnectedEvent;
import io.unifycom.event.DisconnectedEvent;
import io.unifycom.event.exception.UnsupportedEventException;
import io.unifycom.event.handler.ChannelEventHandler;
import io.unifycom.event.result.ChannelEventResult;
import io.unifycom.websocket.event.codec.AbstractTextToEventDecoder;
import org.junit.Test;

public class WebsocketChannelTest {


    @Test
    public void testConnect() throws InterruptedException {

        WebsocketChannelConfig config = new WebsocketChannelConfig("localhost:8080/websocket");

        WebsocketChannel channel = new WebsocketChannel(config, new AbstractTextToEventDecoder() {

            @Override
            public ChannelEvent decode0(String message) {

                System.out.println(message);
                return null;
            }
        });

        channel.addLast(new ChannelEventHandler<ConnectedEvent>() {

            @Override
            public ChannelEventResult onEvent(ConnectedEvent channelEvent) throws UnsupportedEventException {

                System.out.println("WebsocketConnectedEvent");
                return null;
            }
        }, new ChannelEventHandler<DisconnectedEvent>() {

            @Override
            public ChannelEventResult onEvent(DisconnectedEvent channelEvent) throws UnsupportedEventException {

                System.out.println("WebsocketDisconnectedEvent");
                return null;
            }
        });

        channel.connect().blockUntilConnected();

        Thread.sleep(1000 * 60 * 1);
        channel.close();
        Thread.sleep(500 * 60 * 1);
    }
}
