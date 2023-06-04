package io.unifycom.dispatch;

import io.unifycom.Channel;
import io.unifycom.event.ChannelEvent;
import io.unifycom.event.handler.ChannelEventHandler;
import io.unifycom.interceptor.ChannelEventHandlerInterceptor;

public interface ChannelDispatcher {

    void close();

    void fire(Channel channel, Object in);

    void fire(Channel channel, ChannelEvent in);

    void addLast(ChannelEventHandler<?> eventHandler);

    void addLast(ChannelEventHandlerInterceptor<?> eventHandlerInterceptor);
}
