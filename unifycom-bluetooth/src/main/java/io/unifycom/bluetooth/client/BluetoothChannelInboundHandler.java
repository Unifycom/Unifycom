package io.unifycom.bluetooth.client;

import io.unifycom.Ping;
import io.unifycom.dispatch.Dispatcher;
import io.unifycom.netty.client.AbstractSocketChannelInboundHandler;

public class BluetoothChannelInboundHandler extends AbstractSocketChannelInboundHandler {
    public BluetoothChannelInboundHandler(Dispatcher channelEventDispatcher) {

        super(channelEventDispatcher);
    }

    public BluetoothChannelInboundHandler(Dispatcher channelDispatcher, io.unifycom.Channel channel, Ping ping) {

        super(channelDispatcher, channel, ping);
    }
}
