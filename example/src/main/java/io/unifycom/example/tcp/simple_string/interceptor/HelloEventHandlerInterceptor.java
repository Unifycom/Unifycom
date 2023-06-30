package io.unifycom.example.tcp.simple_string.interceptor;

import io.unifycom.Channel;
import io.unifycom.event.handler.ChannelEventHandler;
import io.unifycom.event.result.ChannelEventResult;
import io.unifycom.example.tcp.simple_string.event.HelloEvent;
import io.unifycom.interceptor.ChannelEventHandlerInterceptor;
import io.unifycom.interceptor.InterceptedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloEventHandlerInterceptor implements ChannelEventHandlerInterceptor<HelloEvent> {

    private static final Logger logger = LoggerFactory.getLogger(HelloEventHandlerInterceptor.class);

    @Override
    public boolean preHandle(Channel channel, HelloEvent in, ChannelEventHandler<?> handler) throws Exception {

        logger.debug("preHandle of " + in.getClass());
        return InterceptedResult.CONTINUE;
    }

    @Override
    public boolean postHandle(Channel channel, HelloEvent in, ChannelEventHandler<?> handler, ChannelEventResult result) throws Exception {

        logger.debug("postHandle of " + in.getClass());
        return InterceptedResult.CONTINUE;
    }
}
