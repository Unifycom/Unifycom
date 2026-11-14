package io.unifycom.netty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * The base config of the unifycom server channels whose address is "[host:]port" (tcp/udp), the host is
 * optional so that the connection string could be a bare port.
 */
public abstract class AbstractServerHostPortChannelConfig extends AbstractSocketChannelConfig {

    private int port;
    private String host;

    public AbstractServerHostPortChannelConfig() {

    }

    public AbstractServerHostPortChannelConfig(String connectionString) {

        this.setConnectionString(connectionString);
    }

    public AbstractServerHostPortChannelConfig(int port) {

        this.port = port;
    }

    public AbstractServerHostPortChannelConfig(String host, int port) {

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

        if (StringUtils.isNotBlank(super.getConnectionString())) {

            return super.getConnectionString();
        }

        return StringUtils.isEmpty(host) ? String.valueOf(port) : String.format("%s:%s", host, port);
    }

    @Override
    public void setConnectionString(String connectionString) {

        if (StringUtils.contains(connectionString, ':')) {

            Matcher matcher = Pattern.compile("^([^:]+)?:(\\d+)$").matcher(connectionString);

            if (!matcher.find()) {

                throw new IllegalArgumentException(String.format("Illegal connection string %s.", connectionString));
            }

            this.host = matcher.group(1);
            this.port = NumberUtils.toInt(matcher.group(2));

        } else {

            this.port = NumberUtils.toInt(connectionString);
        }

        if (this.port <= 0) {

            throw new IllegalArgumentException(String.format("Illegal port %s.", connectionString));
        }

        super.setConnectionString(connectionString);
    }

    @Override
    public String toString() {

        return String.format("%s[host = %s, port = %s, %s]",
                this.getClass().getName(), getHost(), getPort(), super.toString());
    }
}
