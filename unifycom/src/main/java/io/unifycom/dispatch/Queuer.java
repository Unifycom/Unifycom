package io.unifycom.dispatch;

import io.unifycom.Channel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Queuer {

    private static final Logger logger = LoggerFactory.getLogger(Queuer.class);

    private ExecutorService executor;
    private final Future<?> future;
    private BlockingQueue<Object> queue = null;

    private volatile boolean stopped = false;

    public Queuer(ExecutorService executor, BiConsumer<Channel, Object> func, int capacity) {

        this(executor, func, 0, capacity);
    }

    public Queuer(ExecutorService executor, BiConsumer<Channel, Object> func, int size4Warning, int capacity) {

        queue = new ArrayBlockingQueue<>(capacity);

        future = executor.submit(() -> {

            while (!stopped) {

                Object in = null;

                try {

                    in = queue.take();
                } catch (Exception e) {

                    logger.warn(e.getMessage(), e);
                }

                if (in != null && in instanceof QueueElement) {

                    QueueElement element = (QueueElement)in;
                    func.accept(element.getChannel(), element.getObject());
                }

                if (size4Warning >= 1 && queue.size() >= size4Warning) {

                    logger.warn("[Queue blocking] queue size:{}.", queue.size());
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

    public void put(QueueElement in) throws InterruptedException {

        try {

            if (!queue.offer(in, 1, TimeUnit.SECONDS)) {

                logger.warn("Offer object to the queue failed, queue size {}", queue.size());
            }
        } catch (InterruptedException e) {

            logger.warn("Queue size {}.", queue.size());
            throw e;
        }
    }

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
