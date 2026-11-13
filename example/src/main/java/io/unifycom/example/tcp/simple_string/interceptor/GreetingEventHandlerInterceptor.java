package io.unifycom.example.tcp.simple_string.interceptor;

import io.unifycom.Channel;
import io.unifycom.event.handler.ChannelEventHandler;
import io.unifycom.event.result.ChannelEventResult;
import io.unifycom.example.tcp.simple_string.event.GreetingEvent;
import io.unifycom.interceptor.EventHandlerInterceptor;
import io.unifycom.interceptor.InterceptedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreetingEventHandlerInterceptor implements EventHandlerInterceptor<GreetingEvent> {

    private static final Logger logger = LoggerFactory.getLogger(GreetingEventHandlerInterceptor.class);

    @Override
    public boolean preHandle(Channel channel, GreetingEvent in, ChannelEventHandler<?> handler) throws Exception {

        logger.debug("preHandle of " + in.getClass());
        return InterceptedResult.CONTINUE;
    }

    @Override
    public boolean postHandle(Channel channel, GreetingEvent in, ChannelEventHandler<?> handler, ChannelEventResult result) throws Exception {

        logger.debug("postHandle of " + in.getClass());
        return InterceptedResult.CONTINUE;
    }
}
