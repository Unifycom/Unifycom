package io.unifycom.dispath;

import io.unifycom.dispatch.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.junit.Test;

public class QueuerTest {

    private static AtomicInteger COUNTER = new AtomicInteger();

    private final ThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern("Queue-" + COUNTER.incrementAndGet() + "-%d")
        .daemon(true).build();
    private ExecutorService executor = new ThreadPoolExecutor(0, 10, 1L, TimeUnit.HOURS, new SynchronousQueue<>(), threadFactory);


    @Test
    public void testPut() throws InterruptedException {

        BlockingQueue q = new BlockingQueue(executor, (c, o) -> {
            System.out.println(o);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 10);

        for (int i = 0; i < 10; i++) {

            q.put(null, "test" + i);
            Thread.sleep(100);
        }

        q.close();

        Thread.sleep(1000);
    }


}
