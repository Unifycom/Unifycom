package io.unifycom.bluetooth.server;

import io.unifycom.dispatch.Dispatcher;
import io.unifycom.netty.server.AbstractNettyChannelRegistry;
import io.unifycom.netty.server.AbstractSocketChannelInboundHandler;

public class BluetoothChannelInboundHandler extends AbstractSocketChannelInboundHandler {

    public BluetoothChannelInboundHandler(Dispatcher channelDispatcher, AbstractNettyChannelRegistry channelHolder) {

        super(channelDispatcher, channelHolder);
    }
}
