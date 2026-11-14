package io.unifycom.udp.client;

import io.unifycom.netty.AbstractHostPortChannelConfig;

public class UdpChannelConfig extends AbstractHostPortChannelConfig {

    public UdpChannelConfig() {

    }

    public UdpChannelConfig(String connectionString) {

        super(connectionString);
    }

    public UdpChannelConfig(String host, int port) {

        super(host, port);
    }
}
