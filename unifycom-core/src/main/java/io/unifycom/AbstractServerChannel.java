package io.unifycom;


import io.unifycom.dispatch.Dispatcher;
import io.unifycom.event.handler.ChannelEventHandler;
import io.unifycom.interceptor.EventHandlerInterceptor;

public abstract class AbstractServerChannel implements ServerChannel {

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
    public AbstractServerChannel addLast(EventHandlerInterceptor<?> eventHandlerInterceptor) {

        channelDispatcher.addLast(eventHandlerInterceptor);
        return this;
    }

    @Override
    public AbstractServerChannel addLast(EventHandlerInterceptor<?>... eventHandlerInterceptors) {

        for (EventHandlerInterceptor<?> eventHandlerInterceptor : eventHandlerInterceptors) {

            this.addLast(eventHandlerInterceptor);
        }

        return this;
    }
}
