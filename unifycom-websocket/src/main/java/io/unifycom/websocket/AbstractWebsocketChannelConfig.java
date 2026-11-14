package io.unifycom.websocket;

import io.unifycom.netty.AbstractSocketChannelConfig;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * The base config of the websocket channels (client/server), the address is "host:port/path".
 */
public abstract class AbstractWebsocketChannelConfig extends AbstractSocketChannelConfig {

    private int port;
    private String host;

    private String path;

    private int maxFramePayloadLength = 65536;

    public AbstractWebsocketChannelConfig() {

    }

    public AbstractWebsocketChannelConfig(String connectionString) {

        this.setConnectionString(connectionString);
    }

    public AbstractWebsocketChannelConfig(int port) {

        this.port = port;
    }

    public AbstractWebsocketChannelConfig(String host, int port) {

        this.port = port;
        this.host = host;
    }

    public AbstractWebsocketChannelConfig(String host, int port, String path) {

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

    /**
     * The maximum size of a WebSocket frame payload, in bytes. Frames whose payload exceeds this
     * limit will fail to be received or aggregated. Defaults to 65536.
     */
    public int getMaxFramePayloadLength() {

        return maxFramePayloadLength;
    }

    public void setMaxFramePayloadLength(int maxFramePayloadLength) {

        this.maxFramePayloadLength = maxFramePayloadLength;
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
