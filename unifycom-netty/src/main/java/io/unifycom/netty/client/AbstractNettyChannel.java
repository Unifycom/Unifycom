package io.unifycom.netty.client;

import io.unifycom.AbstractChannel;
import io.unifycom.AbstractChannelConfig;
import io.unifycom.Channel;
import io.unifycom.Ping;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractNettyChannel extends AbstractChannel {

    private static final Logger logger = LoggerFactory.getLogger(AbstractNettyChannel.class);

    protected Ping ping;
    protected CountDownLatch lock;

    protected AbstractChannelConfig config;

    protected Bootstrap bootstrap;
    protected io.netty.channel.Channel channel;

    @Override
    public synchronized void close() {

        if (channel != null) {

            channel.close().awaitUninterruptibly(100);
        }

        if (channelDispatcher != null) {

            channelDispatcher.close();
        }

        if (lock != null) {

            lock.countDown();
        }

        channel = null;
        bootstrap = null;
        logger.info("{}{} has been closed.", getId(), StringUtils.isNotBlank(getName()) ? ("[" + getName() + "]") : StringUtils.EMPTY);
    }

    @Override
    public boolean isClosed() {

        return bootstrap == null;
    }

    protected synchronized void connect0() {

        if (isActive() || isClosed()) {

            return;
        }

        bootstrap.connect().addListener((ChannelFutureListener) futureListener -> {

            if (futureListener.isSuccess()) {

                lock.countDown();

                if (channel != null) {

                    channel.close();
                }

                channel = futureListener.channel();
            } else {
                logger.warn("{} is not active, try again after {}s.", getId(), config.getAutoConnectIntervalSeconds());
            }
        });
    }

    @Override
    public boolean isActive() {

        return bootstrap != null && channel != null && channel.isActive();
    }

    @Override
    public Future<Void> send(Object out) throws IOException {

        if (!isActive()) {

            throw new IOException("The channel is not ready yet");
        }

        return channel.writeAndFlush(out);
    }

    @Override
    public Channel blockUntilConnected() throws InterruptedException {

        lock.await();

        return this;
    }

    @Override
    public Channel blockUntilConnected(int timeout, TimeUnit unit) throws InterruptedException {

        lock.await(timeout, unit);

        return this;
    }

    public void setPing(Ping ping) {

        this.ping = ping;
    }
}
