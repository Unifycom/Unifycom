package io.unifycom.websocket.server;

import io.unifycom.event.ChannelEvent;
import io.unifycom.event.ConnectedEvent;
import io.unifycom.event.DisconnectedEvent;
import io.unifycom.event.exception.UnsupportedEventException;
import io.unifycom.event.handler.ChannelEventHandler;
import io.unifycom.event.result.ChannelEventResult;
import io.unifycom.websocket.event.codec.AbstractTextToEventDecoder;
import org.junit.Test;

public class WebsocketServerChannelTest {


    @Test
    public void testStartup() throws InterruptedException {

        WebsocketServerChannelConfig config = new WebsocketServerChannelConfig("localhost:8080/websocket");
        WebsocketServerChannel channel = new WebsocketServerChannel(config, new AbstractTextToEventDecoder() {

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

        channel.startup();

        Thread.sleep(1000 * 60 * 10);
        channel.shutdown();
    }

}
