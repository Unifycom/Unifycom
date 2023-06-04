package io.unifycom;

import io.unifycom.dispatch.ChannelDispatcher;
import io.unifycom.event.handler.ChannelEventHandler;
import io.unifycom.interceptor.ChannelEventHandlerInterceptor;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractChannel implements Channel {

    private String name;

    protected ChannelDispatcher channelDispatcher;

    @Override
    public String getName() {

        return StringUtils.defaultIfEmpty(this.name, getId());
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

    public AbstractChannel addLast(ChannelEventHandlerInterceptor<?> eventHandlerInterceptor) {

        channelDispatcher.addLast(eventHandlerInterceptor);
        return this;
    }

    public AbstractChannel addLast(ChannelEventHandlerInterceptor<?>... eventHandlerInterceptors) {

        for (ChannelEventHandlerInterceptor<?> eventHandlerInterceptor : eventHandlerInterceptors) {

            this.addLast(eventHandlerInterceptor);
        }

        return this;
    }
}
