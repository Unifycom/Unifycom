package io.unifycom.udp.client;

import io.netty.channel.ChannelHandlerContext;
import io.unifycom.Channel;
import io.unifycom.Ping;
import io.unifycom.dispatch.Dispatcher;
import io.unifycom.netty.client.AbstractSocketChannelInboundHandler;
import java.net.SocketAddress;

/**
 * The inbound handler of the udp client, it differs from the socket client handler only in that the udp channel
 * is bound locally so the local address is used as the peer address.
 */
public class UdpChannelInboundHandler extends AbstractSocketChannelInboundHandler {

    public UdpChannelInboundHandler(Dispatcher channelEventDispatcher) {

        super(channelEventDispatcher);
    }

    public UdpChannelInboundHandler(Dispatcher channelDispatcher, Channel channel, Ping ping) {

        super(channelDispatcher, channel, ping);
    }

    @Override
    protected SocketAddress peerAddress(ChannelHandlerContext ctx) {

        return ctx.channel().localAddress();
    }
}
