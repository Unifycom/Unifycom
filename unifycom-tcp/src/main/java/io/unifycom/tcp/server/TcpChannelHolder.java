package io.unifycom.tcp.server;


import io.unifycom.socket.server.AbstractSocketChannelHolder;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

class TcpChannelHolder extends AbstractSocketChannelHolder {

    @Override
    protected String getKey(SocketAddress socketAddress) {

        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        return inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort();
    }
}
