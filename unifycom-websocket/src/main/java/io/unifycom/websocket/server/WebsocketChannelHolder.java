package io.unifycom.websocket.server;

import io.undertow.websockets.core.WebSocketChannel;
import io.unifycom.AbstractChannelHolder;
import io.unifycom.Channel;
import java.net.InetSocketAddress;

import org.apache.commons.lang3.StringUtils;

class WebsocketChannelHolder extends AbstractChannelHolder {

    public Channel get(WebSocketChannel channel) {

        String key = generateKey(channel);
        return channels.get(key);
    }

    public Channel remove(WebSocketChannel channel) {

        String key = generateKey(channel);
        return channels.remove(key);
    }

    private static String generateKey(WebSocketChannel channel) {

        return generateKey(channel.getSourceAddress());
    }

    private static String generateKey(InetSocketAddress socketAddress) {

        return socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort();
    }

    public String getKey(Channel channel) {

        if (!(channel instanceof WebsocketSessionChannel)) {

            return StringUtils.EMPTY;
        }

        WebsocketSessionChannel wsClientChannel = (WebsocketSessionChannel)channel;
        WebSocketChannel wsChannel = wsClientChannel.channel();

        return generateKey(wsChannel);
    }
}
