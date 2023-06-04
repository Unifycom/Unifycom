package io.unifycom.websocket.server;

import io.unifycom.AbstractChannelGroup;
import io.unifycom.Channel;
import io.undertow.websockets.core.WebSocketChannel;
import java.net.InetSocketAddress;

import org.apache.commons.lang3.StringUtils;

class WebsocketChannelGroup extends AbstractChannelGroup {

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

    public String generateKey(Channel channel) {

        if (!(channel instanceof WebsocketClientChannel)) {

            return StringUtils.EMPTY;
        }

        WebsocketClientChannel wsClientChannel = (WebsocketClientChannel)channel;
        WebSocketChannel wsChannel = wsClientChannel.channel();

        return generateKey(wsChannel);
    }
}
