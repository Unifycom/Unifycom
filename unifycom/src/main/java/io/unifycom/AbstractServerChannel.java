package io.unifycom;


import io.unifycom.dispatch.ChannelDispatcher;
import io.unifycom.event.handler.ChannelEventHandler;
import io.unifycom.interceptor.ChannelEventHandlerInterceptor;
import java.io.IOException;
import java.util.concurrent.Future;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractServerChannel {

    private String name;

    protected ChannelDispatcher channelDispatcher;

    public abstract String getId();

    public String getName() {

        return StringUtils.defaultIfBlank(this.name, getId());
    }

    public void setName(String name) {

        this.name = name;
    }

    public abstract AbstractServerChannel startup();

    public abstract void shutdown();

    public abstract boolean isReady();

    public abstract Future<Void> send(String channelName, Object out) throws IOException;

    public abstract Channel getClient(String channelName);

    public AbstractServerChannel addLast(ChannelEventHandler<?> eventHandler) {

        channelDispatcher.addLast(eventHandler);
        return this;
    }

    public AbstractServerChannel addLast(ChannelEventHandler<?>... eventHandlers) {

        for (ChannelEventHandler<?> eventHandler : eventHandlers) {

            this.addLast(eventHandler);
        }

        return this;
    }

    public AbstractServerChannel addLast(ChannelEventHandlerInterceptor<?> eventHandlerInterceptor) {

        channelDispatcher.addLast(eventHandlerInterceptor);
        return this;
    }

    public AbstractServerChannel addLast(ChannelEventHandlerInterceptor<?>... eventHandlerInterceptors) {

        for (ChannelEventHandlerInterceptor<?> eventHandlerInterceptor : eventHandlerInterceptors) {

            this.addLast(eventHandlerInterceptor);
        }

        return this;
    }
}
