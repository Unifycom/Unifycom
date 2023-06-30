package io.unifycom.example.tcp.json_string.handler;

import io.unifycom.event.ConnectedEvent;
import io.unifycom.event.exception.UnsupportedEventException;
import io.unifycom.event.handler.AbstractChannelEventHandler;
import io.unifycom.event.result.ChannelEventResult;

public class ConnectedEventHandler extends AbstractChannelEventHandler<ConnectedEvent> {

    @Override
    public ChannelEventResult onEvent0(ConnectedEvent event) throws UnsupportedEventException {

        System.out.println("connected: " + event.getRemoteAddress());

        return ChannelEventResult.NoReply;
    }
}
