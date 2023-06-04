package io.unifycom;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface Channel {

    String getId();

    String getName();

    void setName(String name);

    void close();

    boolean isClosed();

    Channel connect();

    boolean isActive();

    Channel blockUntilConnected() throws InterruptedException;

    Channel blockUntilConnected(int timeout, TimeUnit unit) throws InterruptedException;

    Future<Void> send(Object out) throws IOException;
}
