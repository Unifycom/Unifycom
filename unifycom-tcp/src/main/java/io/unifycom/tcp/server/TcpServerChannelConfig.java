package io.unifycom.tcp.server;

import io.unifycom.netty.AbstractServerHostPortChannelConfig;

public class TcpServerChannelConfig extends AbstractServerHostPortChannelConfig {

    public TcpServerChannelConfig() {

    }

    public TcpServerChannelConfig(String connectionString) {

        super(connectionString);
    }

    public TcpServerChannelConfig(int port) {

        super(port);
    }

    public TcpServerChannelConfig(String host, int port) {

        super(host, port);
    }
}
