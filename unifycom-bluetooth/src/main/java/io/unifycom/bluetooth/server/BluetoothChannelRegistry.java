package io.unifycom.bluetooth.server;

import io.unifycom.netty.server.AbstractNettyChannelRegistry;

import java.net.SocketAddress;

import org.apache.commons.lang3.StringUtils;

class BluetoothChannelRegistry extends AbstractNettyChannelRegistry {

    @Override
    protected String getKey(SocketAddress socketAddress) {

        return socketAddress == null ? StringUtils.EMPTY : socketAddress.toString();
    }
}
