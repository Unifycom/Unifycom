package io.unifycom.udp.server;

import io.unifycom.netty.AbstractServerHostPortChannelConfig;

public class UdpServerChannelConfig extends AbstractServerHostPortChannelConfig {

    public UdpServerChannelConfig() {

    }

    public UdpServerChannelConfig(String connectionString) {

        super(connectionString);
    }

    public UdpServerChannelConfig(int port) {

        super(port);
    }

    public UdpServerChannelConfig(String host, int port) {

        super(host, port);
    }
}
