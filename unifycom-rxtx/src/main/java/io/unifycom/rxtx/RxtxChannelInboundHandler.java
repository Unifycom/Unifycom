package io.unifycom.rxtx;

import io.unifycom.Ping;
import io.unifycom.dispatch.Dispatcher;
import io.unifycom.netty.client.AbstractSocketChannelInboundHandler;
import io.unifycom.Channel;

public class RxtxChannelInboundHandler extends AbstractSocketChannelInboundHandler {

    public RxtxChannelInboundHandler(Dispatcher channelEventDispatcher) {

        super(channelEventDispatcher);
    }

    public RxtxChannelInboundHandler(Dispatcher channelDispatcher, Channel channel, Ping ping) {

        super(channelDispatcher, channel, ping);
    }
}
