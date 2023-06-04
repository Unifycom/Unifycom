package io.unifycom.rxtx;

import io.unifycom.Ping;
import io.unifycom.dispatch.ChannelDispatcher;
import io.unifycom.netty.client.AbstractNettyChannelInboundHandler;
import io.unifycom.Channel;

public class RxtxChannelInboundHandler extends AbstractNettyChannelInboundHandler {
    public RxtxChannelInboundHandler(ChannelDispatcher channelEventDispatcher) {

        super(channelEventDispatcher);
    }

    public RxtxChannelInboundHandler(ChannelDispatcher channelDispatcher, Channel channel, Ping ping) {

        super(channelDispatcher, channel, ping);
    }
}
