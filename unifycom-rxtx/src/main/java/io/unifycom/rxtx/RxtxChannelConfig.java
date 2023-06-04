package io.unifycom.rxtx;

import io.unifycom.netty.AbstractNettyChannelConfig;
import org.apache.commons.lang3.StringUtils;

public class RxtxChannelConfig extends AbstractNettyChannelConfig {

    private String port;
    private int baudrate;
    private int databits = 8;
    private int stopbits = 1;
    private int paritybit = 0;

    public RxtxChannelConfig() {

    }

    public RxtxChannelConfig(String connectionString) {

        this.setConnectionString(connectionString);
    }

    public String getConnectionString() {

        return StringUtils.defaultIfBlank(super.getConnectionString(), port);
    }

    public void setConnectionString(String connectionString) {

        this.port = connectionString;
        super.setConnectionString(connectionString);
    }

    public String getPort() {

        return port;
    }

    public void setPort(String port) {

        this.port = port;
    }

    public int getBaudrate() {

        return baudrate;
    }

    public void setBaudrate(int baudrate) {

        this.baudrate = baudrate;
    }

    public int getDatabits() {

        return databits;
    }

    public void setDatabits(int databits) {

        this.databits = databits;
    }

    public int getStopbits() {

        return stopbits;
    }

    public void setStopbits(int stopbits) {

        this.stopbits = stopbits;
    }

    public int getParitybit() {

        return paritybit;
    }

    public void setParitybit(int paritybit) {

        this.paritybit = paritybit;
    }

    @Override
    public String toString() {

        return String.format("%s[port = %s, baudrate = %s, %s]", this.getClass().getName(), port, baudrate,
                super.toString());
    }
}
