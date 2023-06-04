package io.unifycom.dispatch;

import io.unifycom.Channel;
import io.unifycom.event.ChannelEvent;
import io.unifycom.event.codec.MessageToEventDecoder;
import io.unifycom.event.codec.ResultToMessageEncoder;
import io.unifycom.event.handler.ChannelEventHandler;
import io.unifycom.event.result.ChannelEventResult;
import io.unifycom.event.result.NoReplyEventResult;
import io.unifycom.interceptor.ChannelEventHandlerInterceptor;
import io.unifycom.util.ReflectionUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultChannelDispatcher implements ChannelDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(DefaultChannelDispatcher.class);

    private final MessageToEventDecoder messageToEventDecoder;
    private final ResultToMessageEncoder<?> resultToMessageEncoder;

    private final Map<Class<? extends ChannelEvent>, ChannelEventHandler<?>> eventHandlers = new HashMap<>();
    private final Map<Class<? extends ChannelEvent>, ChannelEventHandlerInterceptor<?>> handlerInterceptors = new HashMap<>();


    public DefaultChannelDispatcher(MessageToEventDecoder<?> messageToEventDecoder, ResultToMessageEncoder<?> resultToMessageEncoder) {

        this.messageToEventDecoder = messageToEventDecoder;
        this.resultToMessageEncoder = resultToMessageEncoder;
    }

    @Override
    public void addLast(ChannelEventHandler<?> eventHandler) {

        Class<? extends ChannelEvent> eventClass = ReflectionUtils.findSubTypeAssignableFromGenericInterface(eventHandler.getClass(),
                                                                                                             ChannelEvent.class);
        assert eventClass != null;
        eventHandlers.put(eventClass, eventHandler);

        logger.debug("Added event to handler mapper {} -> {}.", eventClass.getName(), eventHandler.getClass());
    }

    @Override
    public void addLast(ChannelEventHandlerInterceptor<?> eventHandlerInterceptor) {

        Class<? extends ChannelEvent> eventClass = ReflectionUtils.findSubTypeAssignableFromGenericInterface(eventHandlerInterceptor.getClass(),
                                                                                                             ChannelEvent.class);
        assert eventClass != null;
        handlerInterceptors.put(eventClass, eventHandlerInterceptor);

        logger.debug("Added event to interceptor mapper {} -> {}.", eventClass.getName(), eventHandlerInterceptor.getClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void fire(Channel channel, Object in) {

        long begin = System.currentTimeMillis();

        ChannelEvent event = messageToEventDecoder.decode(in);

        if (event != null) {

            fire(channel, event);
        }

        logger.debug("Fired message {}, spent {}ms.", in, System.currentTimeMillis() - begin);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void fire(Channel channel, ChannelEvent in) {

        ChannelEventHandler eventHandler = eventHandlers.get(in.getClass());
        ChannelEventHandlerInterceptor globalInterceptor = handlerInterceptors.get(ChannelEvent.class);

        if (!preHandle(channel, in, eventHandler, globalInterceptor)) {

            logger.debug("Event has been broken by global pre interceptor {}.", globalInterceptor.getClass());
            return;
        }

        ChannelEventHandlerInterceptor handlerInterceptor = handlerInterceptors.get(in.getClass());

        if (!preHandle(channel, in, eventHandler, handlerInterceptor)) {

            logger.debug("Event has been broken by pre interceptor {}.", handlerInterceptor.getClass());
            return;
        }

        if (eventHandler == null) {

            logger.warn("Not found any event handlers for event {}.", in.getClass());
            return;
        }

        try {

            ChannelEventResult result = eventHandler.onEvent(in);

            if (!postHandle(channel, in, eventHandler, result, handlerInterceptor)) {

                logger.debug("Event has been broken by post interceptor {}.", handlerInterceptor.getClass());
                return;
            }

            if (!postHandle(channel, in, eventHandler, result, globalInterceptor)) {

                logger.debug("Event has been broken by global post interceptor {}.", handlerInterceptor.getClass());
                return;
            }

            send(channel, result);
        } catch (Exception e) {

            logger.error(e.getMessage(), e);
        }
    }

    private boolean preHandle(Channel channel, ChannelEvent in, ChannelEventHandler eventHandler, ChannelEventHandlerInterceptor handlerInterceptor) {

        if (handlerInterceptor == null) {

            return true;
        }

        try {

            long begin = System.currentTimeMillis();
            @SuppressWarnings("unchecked")
            boolean result = handlerInterceptor.preHandle(channel, in, eventHandler);
            logger.debug("Pre handler {} spent {}ms, event: {}", handlerInterceptor.getClass(), System.currentTimeMillis() - begin, in);

            return result;
        } catch (Exception e) {

            logger.error(e.getMessage(), e);
        }

        return false;
    }

    private boolean postHandle(Channel channel, ChannelEvent in, ChannelEventHandler eventHandler, ChannelEventResult eventResult,
                               ChannelEventHandlerInterceptor handlerInterceptor) {

        if (handlerInterceptor == null) {

            return true;
        }

        try {

            long begin = System.currentTimeMillis();
            @SuppressWarnings("unchecked")
            boolean result = handlerInterceptor.postHandle(channel, in, eventHandler, eventResult);
            logger.debug("Post handler {} spent {}ms, event: {}, result: {}", handlerInterceptor.getClass(), System.currentTimeMillis() - begin, in,
                         eventResult);

            return result;
        } catch (Exception e) {

            logger.error(e.getMessage(), e);
        }

        return false;
    }

    private void send(Channel channel, ChannelEventResult result) throws IOException {

        if (result == null) {

            logger.debug("Event result is null, ignored.");
            return;
        }

        if (result instanceof NoReplyEventResult) {

            logger.debug("No reply event result {}, ignored.", result.getClass());
            return;
        }

        Object out = resultToMessageEncoder.encode(result);
        channel.send(out);
    }

    @Override
    public void close() {

        // No need close anything
    }
}
