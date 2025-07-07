package io.unifycom.tcp.server;

import io.unifycom.dispatch.ChannelDispatcher;
import io.unifycom.socket.server.AbstractSocketChannelHolder;
import io.unifycom.socket.server.AbstractSocketChannelInboundHandler;

public class TcpChannelInboundHandler extends AbstractSocketChannelInboundHandler {

    public TcpChannelInboundHandler(ChannelDispatcher channelDispatcher, AbstractSocketChannelHolder nettyChannelGroup) {

        super(channelDispatcher, nettyChannelGroup);
    }
}
