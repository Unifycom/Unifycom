package io.unifycom.tcp.server;


import io.unifycom.netty.server.AbstractNettyChannelRegistry;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

class TcpChannelRegistry extends AbstractNettyChannelRegistry {

    @Override
    protected String getKey(SocketAddress socketAddress) {

        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        return inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort();
    }
}
