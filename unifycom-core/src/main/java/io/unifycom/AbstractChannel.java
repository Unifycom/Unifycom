package io.unifycom;

import io.unifycom.dispatch.Dispatcher;
import io.unifycom.event.handler.ChannelEventHandler;
import io.unifycom.interceptor.EventHandlerInterceptor;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractChannel implements Channel {

    private String name = ChannelNameGenerator.generate(this.getClass());

    protected Dispatcher channelDispatcher;

    @Override
    public String getName() {

        return this.name;
    }

    @Override
    public void setName(String name) {

        this.name = name;
    }

    public AbstractChannel addLast(ChannelEventHandler<?> eventHandler) {

        channelDispatcher.addLast(eventHandler);
        return this;
    }

    public AbstractChannel addLast(ChannelEventHandler<?>... eventHandlers) {

        for (ChannelEventHandler<?> eventHandler : eventHandlers) {

            this.addLast(eventHandler);
        }

        return this;
    }

    public AbstractChannel addLast(EventHandlerInterceptor<?> eventHandlerInterceptor) {

        channelDispatcher.addLast(eventHandlerInterceptor);
        return this;
    }

    public AbstractChannel addLast(EventHandlerInterceptor<?>... eventHandlerInterceptors) {

        for (EventHandlerInterceptor<?> eventHandlerInterceptor : eventHandlerInterceptors) {

            this.addLast(eventHandlerInterceptor);
        }

        return this;
    }
}
