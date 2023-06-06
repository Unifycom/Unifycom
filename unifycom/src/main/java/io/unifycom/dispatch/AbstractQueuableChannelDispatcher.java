package io.unifycom.dispatch;

import io.unifycom.Channel;
import io.unifycom.event.codec.MessageToEventDecoder;
import io.unifycom.event.codec.ResultToMessageEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractQueuableChannelDispatcher extends DefaultChannelDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(AbstractQueuableChannelDispatcher.class);

    private static AtomicInteger EXECUTOR_COUNTER = new AtomicInteger();

    private static final int MAX_QUEUE_COUNT = 10_000;
    private static final int DEFAULT_QUEUE_COUNT = 100;

    private static final int MAX_QUEUE_CAPACITY = 100_000;

    private int queueCount;
    private int queueCapacity;
    private Map<Integer, Queuer> queues;

    private final ExecutorService executor;

    protected AbstractQueuableChannelDispatcher(MessageToEventDecoder messageToEventDecoder, ResultToMessageEncoder resultToMessageEncoder,
                                                int queueCapacity) {

        this(messageToEventDecoder, resultToMessageEncoder, DEFAULT_QUEUE_COUNT, queueCapacity);
    }

    protected AbstractQueuableChannelDispatcher(MessageToEventDecoder messageToEventDecoder, ResultToMessageEncoder resultToMessageEncoder,
                                                int queueCount, int queueCapacity) {

        super(messageToEventDecoder, resultToMessageEncoder);

        if (queueCount <= 0 || queueCount > MAX_QUEUE_COUNT) {

            throw new IllegalArgumentException(String.format("Queue count is over %s.", MAX_QUEUE_COUNT));
        }

        if (queueCapacity <= 0 || queueCapacity > MAX_QUEUE_CAPACITY) {

            throw new IllegalArgumentException(String.format("Queue capacity is over %s.", MAX_QUEUE_CAPACITY));
        }

        this.queueCount = queueCount;
        this.queueCapacity = queueCapacity;
        this.queues = new ConcurrentHashMap<>(queueCount);

        executor = new ThreadPoolExecutor(0, queueCount, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
                                          new BasicThreadFactory.Builder().namingPattern(
                                              Queuer.class.getSimpleName() + "-" + EXECUTOR_COUNTER.incrementAndGet() + "-%d").daemon(true).build());
    }

    @Override
    public void fire(Channel channel, Object in) {

        int g = groupBy(in);

        if (g < 0) {

            throw new IllegalArgumentException("Negative argument " + g);
        }

        Queuer queuer = queues.computeIfAbsent(g % queueCount, k -> new Queuer(executor, (c, o) -> super.fire(c, o), queueCapacity));

        try {

            queuer.put(new QueueElement(channel, in));
        } catch (Exception e) {

            logger.error(e.getMessage(), e);
        }
    }

    public abstract int groupBy(Object in);

    @Override
    public void close() {

        queues.forEach((k, v) -> v.close());
        queues.clear();
    }
}
