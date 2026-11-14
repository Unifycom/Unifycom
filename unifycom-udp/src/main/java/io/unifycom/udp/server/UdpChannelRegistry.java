package io.unifycom.udp.server;

import io.unifycom.Channel;
import io.unifycom.netty.server.AbstractNettyChannelRegistry;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.apache.commons.lang3.StringUtils;

class UdpChannelRegistry extends AbstractNettyChannelRegistry {

    public Channel get(SocketAddress socketAddress) {

        return channels.get(getKey(socketAddress));
    }

    public void remove(SocketAddress socketAddress) {

        channels.remove(getKey(socketAddress));
    }

    @Override
    protected String getKey(SocketAddress socketAddress) {

        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        return inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort();
    }

    @Override
    public String getKey(Channel channel) {

        if (!(channel instanceof UdpSessionChannel)) {

            return StringUtils.EMPTY;
        }

        UdpSessionChannel sessionChannel = (UdpSessionChannel) channel;
        return getKey(sessionChannel.getRemoteAddress());
    }
}
