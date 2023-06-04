package io.unifycom.bluetooth.client;

import io.unifycom.Ping;
import io.unifycom.dispatch.ChannelDispatcher;
import io.unifycom.netty.client.AbstractNettyChannelInboundHandler;

public class BluetoothChannelInboundHandler extends AbstractNettyChannelInboundHandler {
    public BluetoothChannelInboundHandler(ChannelDispatcher channelEventDispatcher) {

        super(channelEventDispatcher);
    }

    public BluetoothChannelInboundHandler(ChannelDispatcher channelDispatcher, io.unifycom.Channel channel, Ping ping) {

        super(channelDispatcher, channel, ping);
    }
}
