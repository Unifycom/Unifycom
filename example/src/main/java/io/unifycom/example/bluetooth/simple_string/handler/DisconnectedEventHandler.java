package io.unifycom.example.bluetooth.simple_string.handler;

import io.unifycom.event.DisconnectedEvent;
import io.unifycom.event.exception.UnsupportedEventException;
import io.unifycom.event.handler.AbstractChannelEventHandler;
import io.unifycom.event.result.ChannelEventResult;

public class DisconnectedEventHandler extends AbstractChannelEventHandler<DisconnectedEvent> {

    @Override
    public ChannelEventResult onEvent0(DisconnectedEvent event) throws UnsupportedEventException {

        System.out.println("disconnected: " + event.getRemoteAddress());

        return ChannelEventResult.NoReply;
    }
}
