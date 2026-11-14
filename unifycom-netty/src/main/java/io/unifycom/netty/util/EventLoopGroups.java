package io.unifycom.netty.util;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;

/**
 * The factory of netty {@link EventLoopGroup}, it picks the epoll implementation on linux, otherwise falls back to nio.
 */
public final class EventLoopGroups {

    private EventLoopGroups() {

    }

    /**
     * Creates an epoll/nio event loop group with the default amount of threads.
     */
    public static EventLoopGroup newDefaultEventLoopGroup() {

        return newDefaultEventLoopGroup(0);
    }

    /**
     * Creates an epoll/nio event loop group with the specified amount of threads.
     */
    public static EventLoopGroup newDefaultEventLoopGroup(int nThreads) {

        return Epoll.isAvailable() ? new EpollEventLoopGroup(nThreads) : new NioEventLoopGroup(nThreads);
    }

    /**
     * Creates an oio event loop group, it is used by the blocking transports (bluetooth/rxtx).
     */
    @SuppressWarnings("deprecation")
    public static EventLoopGroup newOioEventLoopGroup() {

        return new OioEventLoopGroup();
    }

    /**
     * Registers a shutdown hook to shutdown the specified event loop group gracefully.
     */
    public static void shutdownHook(EventLoopGroup eventLoopGroup) {

        Runtime.getRuntime().addShutdownHook(new Thread(eventLoopGroup::shutdownGracefully));
    }
}
