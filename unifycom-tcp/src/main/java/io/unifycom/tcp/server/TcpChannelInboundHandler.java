package io.unifycom.tcp.server;

import io.unifycom.dispatch.Dispatcher;
import io.unifycom.netty.server.AbstractNettyChannelRegistry;
import io.unifycom.netty.server.AbstractSocketChannelInboundHandler;

public class TcpChannelInboundHandler extends AbstractSocketChannelInboundHandler {

    public TcpChannelInboundHandler(Dispatcher channelDispatcher, AbstractNettyChannelRegistry channelHolder) {

        super(channelDispatcher, channelHolder);
    }
}
