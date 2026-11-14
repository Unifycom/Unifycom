package io.unifycom.tcp.client;

import io.unifycom.netty.AbstractHostPortChannelConfig;

public class TcpChannelConfig extends AbstractHostPortChannelConfig {

    public TcpChannelConfig() {

    }

    public TcpChannelConfig(String connectionString) {

        super(connectionString);
    }

    public TcpChannelConfig(String host, int port) {

        super(host, port);
    }
}
