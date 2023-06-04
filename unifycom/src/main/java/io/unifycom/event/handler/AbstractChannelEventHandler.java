package io.unifycom.event.handler;


import io.unifycom.event.ChannelEvent;
import io.unifycom.event.exception.UnsupportedEventException;
import io.unifycom.event.result.ChannelEventResult;

public abstract class AbstractChannelEventHandler<E extends ChannelEvent> implements ChannelEventHandler<E> {

    @Override
    public ChannelEventResult onEvent(E event) throws UnsupportedEventException {

        ChannelEventResult result = onEvent0(event);
        result.setSource(event);

        return result;
    }

    public abstract ChannelEventResult onEvent0(E event) throws UnsupportedEventException;
}
