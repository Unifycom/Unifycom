package io.unifycom.netty.util;

import io.unifycom.event.IdleEvent;
import io.netty.handler.timeout.IdleStateEvent;

public class IdleUtils {

    public static IdleEvent toEvent(IdleStateEvent idleStateEvent) {

        IdleEvent idleEvent = null;

        switch (idleStateEvent.state()) {

            case ALL_IDLE:
                idleEvent = IdleEvent.ALL_IDLE_EVENT;
                break;
            case READER_IDLE:
                idleEvent = IdleEvent.READER_IDLE_EVENT;
                break;
            case WRITER_IDLE:
                idleEvent = IdleEvent.WRITER_IDLE_EVENT;
                break;
            default:
        }

        return idleEvent;
    }
}
