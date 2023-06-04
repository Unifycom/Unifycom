package io.unifycom.tcp.server;


import io.unifycom.netty.server.AbstractNettyChannelGroup;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

class TcpChannelGroup extends AbstractNettyChannelGroup {

    @Override
    protected String generateKey(SocketAddress socketAddress) {

        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        return inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort();
    }
}
