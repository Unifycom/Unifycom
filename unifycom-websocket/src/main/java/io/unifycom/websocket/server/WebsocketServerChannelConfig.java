package io.unifycom.websocket.server;

import io.unifycom.websocket.AbstractWebsocketChannelConfig;

public class WebsocketServerChannelConfig extends AbstractWebsocketChannelConfig {

    public WebsocketServerChannelConfig() {

    }

    public WebsocketServerChannelConfig(String connectionString) {

        super(connectionString);
    }

    public WebsocketServerChannelConfig(int port) {

        super(port);
    }

    public WebsocketServerChannelConfig(String host, int port) {

        super(host, port);
    }

    public WebsocketServerChannelConfig(String host, int port, String path) {

        super(host, port, path);
    }
}
