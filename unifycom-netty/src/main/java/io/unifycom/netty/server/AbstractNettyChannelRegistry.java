package io.unifycom.netty.server;

import io.unifycom.Channel;
import io.unifycom.ChannelRegistry;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.commons.lang3.StringUtils;

/**
 * The registry of the sessions accepted by the unifycom netty server channels, the key is the peer address
 * of the netty channel.
 */
public class AbstractNettyChannelRegistry extends ChannelRegistry {

    public Channel get(io.netty.channel.Channel channel) {

        String key = generateKey(channel);
        return channels.get(key);
    }

    public Channel get(SocketAddress socketAddress) {

        return channels.get(getKey(socketAddress));
    }

    public Channel remove(io.netty.channel.Channel channel) {

        String key = generateKey(channel);
        return channels.remove(key);
    }

    public Channel remove(SocketAddress socketAddress) {

        return channels.remove(getKey(socketAddress));
    }

    private String generateKey(io.netty.channel.Channel channel) {

        return getKey(channel.remoteAddress());
    }

    /**
     * Generates the key of the socket address, the default is the "host:port" of the {@link InetSocketAddress},
     * the transports with another address scheme (bluetooth) override it.
     */
    protected String getKey(SocketAddress socketAddress) {

        if (socketAddress instanceof InetSocketAddress) {

            InetSocketAddress inetSocketAddress = (InetSocketAddress)socketAddress;
            return inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort();
        }

        return socketAddress == null ? StringUtils.EMPTY : socketAddress.toString();
    }

    @Override
    public String getKey(Channel channel) {

        if (!(channel instanceof NettySessionChannel)) {

            return StringUtils.EMPTY;
        }

        return getKey(((NettySessionChannel)channel).keySocketAddress());
    }
}
