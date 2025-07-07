package io.unifycom;


import io.unifycom.dispatch.ChannelDispatcher;
import io.unifycom.event.handler.ChannelEventHandler;
import io.unifycom.interceptor.ChannelEventHandlerInterceptor;

public abstract class AbstractServerChannel implements ServerChannel {

    private String name = ChannelNameGenerator.generate(this.getClass());

    protected ChannelDispatcher channelDispatcher;

    @Override
    public String getName() {

        return this.name;
    }

    @Override
    public void setName(String name) {

        this.name = name;
    }

    @Override
    public AbstractServerChannel addLast(ChannelEventHandler<?> eventHandler) {

        channelDispatcher.addLast(eventHandler);
        return this;
    }

    @Override
    public AbstractServerChannel addLast(ChannelEventHandler<?>... eventHandlers) {

        for (ChannelEventHandler<?> eventHandler : eventHandlers) {

            this.addLast(eventHandler);
        }

        return this;
    }

    @Override
    public AbstractServerChannel addLast(ChannelEventHandlerInterceptor<?> eventHandlerInterceptor) {

        channelDispatcher.addLast(eventHandlerInterceptor);
        return this;
    }

    @Override
    public AbstractServerChannel addLast(ChannelEventHandlerInterceptor<?>... eventHandlerInterceptors) {

        for (ChannelEventHandlerInterceptor<?> eventHandlerInterceptor : eventHandlerInterceptors) {

            this.addLast(eventHandlerInterceptor);
        }

        return this;
    }
}
