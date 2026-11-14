package io.unifycom.websocket.server;

import io.unifycom.Channel;
import io.unifycom.ChannelRegistry;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.commons.lang3.StringUtils;

class WebsocketChannelHolder extends ChannelRegistry {

    public Channel get(io.netty.channel.Channel channel) {

        String key = generateKey(channel);
        return channels.get(key);
    }

    public Channel remove(io.netty.channel.Channel channel) {

        String key = generateKey(channel);
        return channels.remove(key);
    }

    private static String generateKey(io.netty.channel.Channel channel) {

        return generateKey(channel.remoteAddress());
    }

    private static String generateKey(SocketAddress socketAddress) {

        InetSocketAddress inetSocketAddress = (InetSocketAddress)socketAddress;
        return inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort();
    }

    @Override
    public String getKey(Channel channel) {

        if (!(channel instanceof WebsocketSessionChannel)) {

            return StringUtils.EMPTY;
        }

        io.netty.channel.Channel nettyChannel = ((WebsocketSessionChannel)channel).unwrap();

        return generateKey(nettyChannel);
    }
}
