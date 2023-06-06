package io.unifycom.dispath;

import io.unifycom.dispatch.QueueElement;
import io.unifycom.dispatch.Queuer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.junit.Test;

public class QueuerTest {

    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(0, 1000, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
                                                                           new BasicThreadFactory.Builder().namingPattern(
                                                                               Queuer.class.getSimpleName() + "-%d").daemon(true).build());


    @Test
    public void testPut() throws InterruptedException {

        Queuer q = new Queuer(EXECUTOR, (c, o) -> {
            System.out.println(o);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 2, 10);

        for (int i = 0; i < 10; i++) {

            q.put(new QueueElement(null, "test" + i));
            Thread.sleep(100);
        }

        q.close();

        Thread.sleep(1000);
    }


}
