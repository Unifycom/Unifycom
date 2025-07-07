package io.unifycom.tcp.client;

import io.unifycom.Channel;
import io.unifycom.Ping;
import io.unifycom.dispatch.ChannelDispatcher;
import io.unifycom.socket.client.AbstractSocketChannelInboundHandler;

public class TcpChannelInboundHandler extends AbstractSocketChannelInboundHandler {

    public TcpChannelInboundHandler(ChannelDispatcher channelEventDispatcher) {

        super(channelEventDispatcher);
    }

    public TcpChannelInboundHandler(ChannelDispatcher channelDispatcher, Channel channel, Ping ping) {

        super(channelDispatcher, channel, ping);
    }
}
