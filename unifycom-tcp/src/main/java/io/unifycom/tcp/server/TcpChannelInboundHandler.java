package io.unifycom.tcp.server;

import io.unifycom.dispatch.ChannelDispatcher;
import io.unifycom.socket.server.AbstractNettyChannelGroup;
import io.unifycom.socket.server.AbstractNettyChannelInboundHandler;

public class TcpChannelInboundHandler extends AbstractNettyChannelInboundHandler {

    public TcpChannelInboundHandler(ChannelDispatcher channelDispatcher, AbstractNettyChannelGroup nettyChannelGroup) {

        super(channelDispatcher, nettyChannelGroup);
    }
}
