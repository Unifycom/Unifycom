package io.unifycom.interceptor;

import io.unifycom.Channel;
import io.unifycom.event.ChannelEvent;
import io.unifycom.event.handler.ChannelEventHandler;
import io.unifycom.event.result.ChannelEventResult;

public interface ChannelEventHandlerInterceptor<E extends ChannelEvent> {

    /**
     * @return False if break the event handler, else continue,
     */
    boolean preHandle(Channel channel, E in, ChannelEventHandler<?> handler) throws Exception;

    /**
     * @return False if don't send out message, else continue,
     */
    boolean postHandle(Channel channel, E in, ChannelEventHandler<?> handler, ChannelEventResult result) throws Exception;
}
