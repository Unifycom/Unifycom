package io.unifycom;


public interface ChannelConfig {

    String getConnectionString();

    void setConnectionString(String connectionString);

    int getAutoConnectIntervalSeconds();

    void setAutoConnectIntervalSeconds(int autoConnectIntervalSeconds);
}
