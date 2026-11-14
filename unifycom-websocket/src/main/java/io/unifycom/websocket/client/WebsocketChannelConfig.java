package io.unifycom.websocket.client;

import io.unifycom.websocket.AbstractWebsocketChannelConfig;

public class WebsocketChannelConfig extends AbstractWebsocketChannelConfig {

    public WebsocketChannelConfig() {

    }

    public WebsocketChannelConfig(String connectionString) {

        super(connectionString);
    }

    public WebsocketChannelConfig(int port) {

        super(port);
    }

    public WebsocketChannelConfig(String host, int port) {

        super(host, port);
    }

    public WebsocketChannelConfig(String host, int port, String path) {

        super(host, port, path);
    }
}
