package io.unifycom.tcp.client;

import io.unifycom.Channel;
import io.unifycom.Ping;
import io.unifycom.dispatch.Dispatcher;
import io.unifycom.netty.client.AbstractSocketChannelInboundHandler;

public class TcpChannelInboundHandler extends AbstractSocketChannelInboundHandler {

    public TcpChannelInboundHandler(Dispatcher channelEventDispatcher) {

        super(channelEventDispatcher);
    }

    public TcpChannelInboundHandler(Dispatcher channelDispatcher, Channel channel, Ping ping) {

        super(channelDispatcher, channel, ping);
    }
}
