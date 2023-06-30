package io.unifycom.example.tcp.simple_string.interceptor;

import io.unifycom.Channel;
import io.unifycom.event.ChannelEvent;
import io.unifycom.event.handler.ChannelEventHandler;
import io.unifycom.event.result.ChannelEventResult;
import io.unifycom.interceptor.ChannelEventHandlerInterceptor;
import io.unifycom.interceptor.InterceptedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalEventHandlerInterceptor implements ChannelEventHandlerInterceptor<ChannelEvent> {

    private static final Logger logger = LoggerFactory.getLogger(GlobalEventHandlerInterceptor.class);

    @Override
    public boolean preHandle(Channel channel, ChannelEvent in, ChannelEventHandler<?> handler) throws Exception {

        logger.debug("Global preHandle of " + in.getClass());
        return InterceptedResult.CONTINUE;
    }

    @Override
    public boolean postHandle(Channel channel, ChannelEvent in, ChannelEventHandler<?> handler, ChannelEventResult result) throws Exception {

        logger.debug("Global postHandle of " + in.getClass());
        return InterceptedResult.CONTINUE;
    }
}
