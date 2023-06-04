package io.unifycom.dispath;

import io.unifycom.dispatch.QueueElement;
import io.unifycom.dispatch.Queuer;

import org.junit.Test;

public class QueuerTest {


    @Test
    public void testPut() throws InterruptedException {

        Queuer q = new Queuer((c, o) -> {
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
