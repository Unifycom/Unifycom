package io.unifycom;

public abstract class AbstractChannelConfig implements ChannelConfig {

    private String connectionString;
    private int pingIntervalSeconds = 5;
    private int autoConnectIntervalSeconds = 10;

    public String getConnectionString() {

        return connectionString;
    }

    public void setConnectionString(String connectionString) {

        this.connectionString = connectionString;
    }

    public int getPingIntervalSeconds() {

        return pingIntervalSeconds;
    }

    public void setPingIntervalSeconds(int pingIntervalSeconds) {

        this.pingIntervalSeconds = pingIntervalSeconds;
    }

    public int getAutoConnectIntervalSeconds() {

        return autoConnectIntervalSeconds;
    }

    public void setAutoConnectIntervalSeconds(int autoConnectIntervalSeconds) {

        this.autoConnectIntervalSeconds = autoConnectIntervalSeconds;
    }

    @Override
    public String toString() {

        return String.format("connectionString = %s, pingIntervalSeconds = %s, autoConnectIntervalSeconds = %s",
                connectionString, pingIntervalSeconds, autoConnectIntervalSeconds);
    }
}
