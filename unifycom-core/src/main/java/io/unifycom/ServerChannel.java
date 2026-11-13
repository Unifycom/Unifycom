package io.unifycom;


import io.unifycom.event.handler.ChannelEventHandler;
import io.unifycom.interceptor.EventHandlerInterceptor;

import java.io.IOException;
import java.util.concurrent.Future;

public interface ServerChannel {

    String getName();

    void setName(String name);

    ServerChannel startup();

    void shutdown();

    boolean isReady();

    Future<Void> send(String channelName, Object out) throws IOException;

    Channel getChannel(String name);

    ServerChannel addLast(ChannelEventHandler<?> eventHandler);

    ServerChannel addLast(ChannelEventHandler<?>... eventHandlers);

    ServerChannel addLast(EventHandlerInterceptor<?> eventHandlerInterceptor);

    ServerChannel addLast(EventHandlerInterceptor<?>... eventHandlerInterceptors);
}
