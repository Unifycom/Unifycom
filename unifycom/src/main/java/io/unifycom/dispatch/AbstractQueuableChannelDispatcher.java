package io.unifycom.dispatch;

import io.unifycom.Channel;
import io.unifycom.event.codec.MessageToEventDecoder;
import io.unifycom.event.codec.ResultToMessageEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractQueuableChannelDispatcher extends DefaultChannelDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(AbstractQueuableChannelDispatcher.class);

    private static final int MAX_QUEUE_COUNT = 10_000;
    private static final int DEFAULT_QUEUE_COUNT = 100;

    private static final int MAX_QUEUE_CAPACITY = 100_000;

    private int queueCount;
    private int queueCapacity;
    private Map<Integer, Queuer> queues;

    protected AbstractQueuableChannelDispatcher(MessageToEventDecoder messageToEventDecoder, ResultToMessageEncoder resultToMessageEncoder) {

        this(messageToEventDecoder, resultToMessageEncoder, DEFAULT_QUEUE_COUNT);
    }

    protected AbstractQueuableChannelDispatcher(MessageToEventDecoder messageToEventDecoder, ResultToMessageEncoder resultToMessageEncoder,
                                                int queueCount, int queueCapacity) {

        this(messageToEventDecoder, resultToMessageEncoder, queueCount);

        if (queueCapacity <= 0 || queueCapacity > MAX_QUEUE_CAPACITY) {

            throw new IllegalArgumentException(String.format("Queue capacity is over %s.", MAX_QUEUE_CAPACITY));
        }

        this.queueCapacity = queueCapacity;
    }

    protected AbstractQueuableChannelDispatcher(MessageToEventDecoder messageToEventDecoder, ResultToMessageEncoder resultToMessageEncoder,
                                                int queueCount) {

        super(messageToEventDecoder, resultToMessageEncoder);

        if (queueCount <= 0 || queueCount > MAX_QUEUE_COUNT) {

            throw new IllegalArgumentException(String.format("Queue count is over %s.", MAX_QUEUE_COUNT));
        }

        this.queueCount = queueCount;
        this.queues = new ConcurrentHashMap<>(queueCount);
    }

    @Override
    public void fire(Channel channel, Object in) {

        int g = groupBy(in);
        Queuer queuer = queues.computeIfAbsent(g % queueCount, k -> new Queuer((c, o) -> super.fire(c, o), queueCapacity));

        try {

            queuer.put(new QueueElement(channel, in));
        } catch (Exception e) {

            logger.error(e.getMessage(), e);
        }
    }

//    public void dequeue(Channel channel, Object in) {
//
//        super.fire(channel, in);
//    }

    public abstract int groupBy(Object in);

    @Override
    public void close() {

        queues.forEach((k, v) -> v.close());
        queues.clear();
    }
}
