package io.unifycom.netty.server;

import io.unifycom.Channel;
import io.unifycom.AbstractChannelGroup;

import java.net.SocketAddress;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractNettyChannelGroup extends AbstractChannelGroup {

    public Channel get(io.netty.channel.Channel channel) {

        String key = generateKey(channel);
        return channels.get(key);
    }

    public Channel remove(io.netty.channel.Channel channel) {

        String key = generateKey(channel);
        return channels.remove(key);
    }

    private String generateKey(io.netty.channel.Channel channel) {

        return generateKey(channel.remoteAddress());
    }

    protected abstract String generateKey(SocketAddress socketAddress);

    public String generateKey(Channel channel) {

        if (!(channel instanceof NettyChannel)) {

            return StringUtils.EMPTY;
        }

        NettyChannel clientChannel = (NettyChannel) channel;
        io.netty.channel.Channel nettyChannel = clientChannel.channel();

        return generateKey(nettyChannel);
    }
}
