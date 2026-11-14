package io.unifycom.websocket.server;

import io.netty.util.AttributeKey;
import io.unifycom.netty.server.NettySessionChannel;
import io.unifycom.websocket.util.WebsocketFrames;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * The session channel of the websocket server, the channel name is stored in the attribute of the netty channel,
 * and the outbound message is wrapped into a websocket frame.
 */
public class WebsocketSessionChannel extends NettySessionChannel {

    private static final AttributeKey<String> ATTR_CHANNEL_NAME = AttributeKey.valueOf("X_ATTR.CHANNEL_NAME");

    public WebsocketSessionChannel(io.netty.channel.Channel channel) {

        super(channel);
    }

    @Override
    public String getName() {

        return Objects.toString(unwrap().attr(ATTR_CHANNEL_NAME).get(), StringUtils.EMPTY);
    }

    @Override
    public void setName(String name) {

        assert StringUtils.isNotBlank(name);
        unwrap().attr(ATTR_CHANNEL_NAME).set(name);
    }

    @Override
    protected Object wrapOutbound(Object out) {

        return WebsocketFrames.toFrame(out);
    }
}
