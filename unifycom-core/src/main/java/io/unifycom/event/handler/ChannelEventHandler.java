package io.unifycom.event.handler;

import io.unifycom.event.ChannelEvent;
import io.unifycom.event.exception.UnsupportedEventException;
import io.unifycom.event.result.ChannelEventResult;

public interface ChannelEventHandler<E extends ChannelEvent> {

    ChannelEventResult onEvent(E event) throws Exception;
}
