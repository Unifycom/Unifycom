package io.unifycom.tcp.client;

import io.unifycom.Ping;
import io.unifycom.dispatch.ChannelDispatcher;
import io.unifycom.netty.client.AbstractNettyChannelInboundHandler;
import io.unifycom.Channel;

public class TcpChannelInboundHandler extends AbstractNettyChannelInboundHandler {
    public TcpChannelInboundHandler(ChannelDispatcher channelEventDispatcher) {

        super(channelEventDispatcher);
    }

    public TcpChannelInboundHandler(ChannelDispatcher channelDispatcher, Channel channel, Ping ping) {

        super(channelDispatcher, channel, ping);
    }
}
