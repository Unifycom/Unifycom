package io.unifycom.dispatch;

import io.unifycom.Channel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoundedMessageQueue implements MessageQueue {

    private static final Logger logger = LoggerFactory.getLogger(BoundedMessageQueue.class);

    private ExecutorService executor;
    private final Future<?> future;
    private final BlockingQueue<Object> queue;

    private volatile boolean stopped = false;

    public BoundedMessageQueue(ExecutorService executor, BiConsumer<Channel, Object> func, int capacity) {

        queue = new ArrayBlockingQueue<>(capacity);

        future = executor.submit(() -> {

            while (!stopped) {

                Object in = null;

                try {

                    in = queue.take();
                } catch (Exception e) {

                    logger.warn(e.getMessage(), e);
                }

                if (in != null && in instanceof Element) {

                    Element element = (Element)in;
                    func.accept(element.getChannel(), element.getObject());
                }
            }

            if (!queue.isEmpty()) {

                logger.warn("{} elements haven't been handled.", queue.size());

                if (logger.isDebugEnabled()) {

                    queue.forEach(element -> logger.debug("+ {}", element));
                }
            }

            logger.info("Stopped.");
        });
    }

    public void put(Channel channel, Object object) throws InterruptedException {

        try {

            if (!queue.offer(new Element(channel, object), 1, TimeUnit.SECONDS)) {

                logger.warn("Offer object to the queue failed, queue size {}", queue.size());
            }
        } catch (InterruptedException e) {

            logger.warn("Queue size {}.", queue.size());
            throw e;
        }
    }

    @Override
    public void close() {

        stopped = true;
        queue.add(new Object());

        try {

            future.get();
        } catch (Exception e) {

            future.cancel(true);
        }
    }
}
