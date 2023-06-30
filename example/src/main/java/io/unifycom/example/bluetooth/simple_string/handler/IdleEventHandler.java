package io.unifycom.example.bluetooth.simple_string.handler;

import io.unifycom.event.IdleEvent;
import io.unifycom.event.exception.UnsupportedEventException;
import io.unifycom.event.handler.AbstractChannelEventHandler;
import io.unifycom.event.result.ChannelEventResult;

public class IdleEventHandler extends AbstractChannelEventHandler<IdleEvent> {

    @Override
    public ChannelEventResult onEvent0(IdleEvent event) throws UnsupportedEventException {

        System.out.println("Idle: " + event.getIdle());

        return ChannelEventResult.NoReply;
    }
}
