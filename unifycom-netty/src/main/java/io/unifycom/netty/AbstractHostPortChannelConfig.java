package io.unifycom.netty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * The base config of the unifycom client channels whose address is "host:port" (tcp/udp).
 */
public abstract class AbstractHostPortChannelConfig extends AbstractSocketChannelConfig {

    private int port;
    private String host;

    public AbstractHostPortChannelConfig() {

    }

    public AbstractHostPortChannelConfig(String connectionString) {

        this.setConnectionString(connectionString);
    }

    public AbstractHostPortChannelConfig(String host, int port) {

        this.port = port;
        this.host = host;
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

    @Override
    public String getConnectionString() {

        return StringUtils.defaultIfBlank(super.getConnectionString(), String.format("%s:%s", host, port));
    }

    @Override
    public void setConnectionString(String connectionString) {

        Matcher matcher = Pattern.compile("^(.+):(\\d+)$").matcher(connectionString);

        if (!matcher.find()) {

            throw new IllegalArgumentException(String.format("Illegal connection string %s.", connectionString));
        }

        try {

            this.port = Integer.parseInt(matcher.group(2));
        } catch (Exception e) {

            throw new IllegalArgumentException(String.format("Illegal port in connection string %s.", connectionString), e);
        }

        this.host = matcher.group(1);
        super.setConnectionString(connectionString);
    }

    @Override
    public String toString() {

        return String.format("%s[host = %s, port = %s, %s]", this.getClass().getName(),
                getHost(), getPort(), super.toString());
    }
}
