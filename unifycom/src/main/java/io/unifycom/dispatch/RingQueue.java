package io.unifycom.dispatch;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.unifycom.Channel;
import java.util.concurrent.ThreadFactory;
import java.util.function.BiConsumer;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RingQueue implements Queue {

    private static final Logger logger = LoggerFactory.getLogger(RingQueue.class);

    private final Disruptor<Element> queue;


    public RingQueue(ThreadFactory threadFactory, BiConsumer<Channel, Object> func, int capacity) {

        if (Integer.bitCount(capacity) != 1) {

            throw new IllegalArgumentException("Ring queue capacity must be a power of 2.");
        }

        queue = new Disruptor(Element::new, capacity, threadFactory, ProducerType.MULTI, new BlockingWaitStrategy());

        queue.handleEventsWith((element, sequence, endOfBatch) -> func.accept(element.getChannel(), element.getObject()));
        queue.setDefaultExceptionHandler(new ExceptionHandler<Element>() {

            @Override
            public void handleEventException(Throwable e, long sequence, Element event) {

                logger.error("Handing event failed.", e);
            }

            @Override
            public void handleOnStartException(Throwable e) {

                logger.error("Starting failed.", e);
            }

            @Override
            public void handleOnShutdownException(Throwable e) {

                logger.error("Shutdown failed.", e);
            }
        });

        queue.start();
    }

    @Override
    public void put(Channel channel, Object object) {

        queue.publishEvent((element, sequence, ch, obj) -> {

            element.setChannel(ch);
            element.setObject(obj);
        }, channel, object);
    }

    @Override
    public void close() {

        queue.shutdown();
        logger.info("Stopped.");
    }
}
