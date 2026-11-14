package io.unifycom.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;

/**
 * An {@link IdleStateHandler} which schedules a reconnect action on the event loop when the channel is unregistered,
 * it is the common way of the unifycom client channels to auto reconnect.
 */
public class AutoReconnectIdleStateHandler extends IdleStateHandler {

    private final int autoConnectIntervalSeconds;
    private final Runnable reconnect;

    public AutoReconnectIdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds,
                                         int autoConnectIntervalSeconds, Runnable reconnect) {

        super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);

        this.autoConnectIntervalSeconds = autoConnectIntervalSeconds;
        this.reconnect = reconnect;
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

        ctx.channel().eventLoop().schedule(reconnect, autoConnectIntervalSeconds, TimeUnit.SECONDS);
    }
}
