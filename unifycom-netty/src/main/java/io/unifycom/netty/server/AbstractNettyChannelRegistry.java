package io.unifycom.netty.server;

import io.unifycom.Channel;
import io.unifycom.ChannelRegistry;

import java.net.SocketAddress;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractNettyChannelRegistry extends ChannelRegistry {

    public Channel get(io.netty.channel.Channel channel) {

        String key = generateKey(channel);
        return channels.get(key);
    }

    public Channel remove(io.netty.channel.Channel channel) {

        String key = generateKey(channel);
        return channels.remove(key);
    }

    private String generateKey(io.netty.channel.Channel channel) {

        return getKey(channel.remoteAddress());
    }

    protected abstract String getKey(SocketAddress socketAddress);

    @Override
    public String getKey(Channel channel) {

        if (!(channel instanceof NettySessionChannel)) {

            return StringUtils.EMPTY;
        }

        NettySessionChannel clientChannel = (NettySessionChannel) channel;
        io.netty.channel.Channel nettyChannel = clientChannel.unwrap();

        return generateKey(nettyChannel);
    }
}
