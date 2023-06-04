package io.unifycom.websocket.client;


import io.unifycom.AbstractChannelConfig;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class WebsocketChannelConfig extends AbstractChannelConfig {

    private int port;
    private String host;

    private String path;

    public WebsocketChannelConfig() {

    }

    public WebsocketChannelConfig(String connectionString) {

        this.setConnectionString(connectionString);
    }

    public WebsocketChannelConfig(int port) {

        this.port = port;
    }

    public WebsocketChannelConfig(String host, int port) {

        this.port = port;
        this.host = host;
    }

    public WebsocketChannelConfig(String host, int port, String path) {

        this.port = port;
        this.host = host;
        this.path = path;
    }

    public int getPort() {

        return port;
    }

    public void setPort(int port) {

        this.port = port;
    }

    public String getHost() {

        return host;
    }

    public void setHost(String host) {

        this.host = host;
    }

    public String getPath() {

        return path;
    }

    public void setPath(String path) {

        this.path = path;
    }

    @Override
    public String getConnectionString() {

        if (StringUtils.isNotBlank(super.getConnectionString())) {

            return super.getConnectionString();
        }

        return String.format("%s%s/%s", StringUtils.isBlank(host) ? StringUtils.EMPTY : (host + ":"), port, path);
    }

    @Override
    public void setConnectionString(String connectionString) {

        Matcher matcher = Pattern.compile("^([^:/\\\\]+):(\\d+)([/\\\\]{1}.+)$").matcher(connectionString);

        if (!matcher.find()) {

            throw new IllegalArgumentException(String.format("Illegal connection string %s.", connectionString));
        }

        this.host = matcher.group(1);
        this.port = NumberUtils.toInt(matcher.group(2));
        this.path = matcher.group(3);

        if (this.port <= 0) {

            throw new IllegalArgumentException(String.format("Illegal port %s.", connectionString));
        }

        super.setConnectionString(connectionString);
    }

    @Override
    public String toString() {

        return String.format("%s[host = %s, port = %s, path = %s]", this.getClass().getName(), getHost(), getPort(), getPath());
    }
}
