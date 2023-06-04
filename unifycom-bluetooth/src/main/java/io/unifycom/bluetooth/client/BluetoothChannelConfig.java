package io.unifycom.bluetooth.client;

import io.unifycom.netty.AbstractNettyChannelConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BluetoothChannelConfig extends AbstractNettyChannelConfig {

    private String address;
    private int channel = 1;
    private boolean authenticate = false;
    private boolean encrypt = false;
    private boolean master = false;
    private String protocol = "btspp";

    public BluetoothChannelConfig() {

    }

    public BluetoothChannelConfig(String connectionString) {

        this.setConnectionString(connectionString);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public boolean isAuthenticate() {
        return authenticate;
    }

    public void setAuthenticate(boolean authenticate) {
        this.authenticate = authenticate;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

    public boolean isMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    public String getConnectionString() {

        return StringUtils.defaultIfBlank(super.getConnectionString(),
                String.format("%s://%s:%s;authenticate=%s;encrypt=%s;master=%s", protocol, address, channel,
                        authenticate, encrypt, master));
    }

    public void setConnectionString(String connectionString) {

        Matcher matcher1 = Pattern.compile("^([^:]+):\\/\\/([^:]+).*$").matcher(connectionString);

        if (!matcher1.find()) {

            throw new IllegalArgumentException(String.format("Illegal connection string %s.", connectionString));
        }

        String protocol = matcher1.group(1);
        String address = matcher1.group(2);

        Matcher matcher2 = Pattern.compile("^.+:(\\d+).*$").matcher(connectionString);

        if (matcher2.find()) {

            try {

                this.channel = NumberUtils.toInt(matcher2.group(1));
            } catch (Exception e) {

                throw new IllegalArgumentException(String.format("Illegal channel in connection string %s.", connectionString), e);
            }
        }

        this.address = address;
        this.protocol = protocol;

        this.master = extract("master", connectionString);
        this.encrypt = extract("encrypt", connectionString);
        this.authenticate = extract("authenticate", connectionString);

        super.setConnectionString(connectionString);
    }

    private static boolean extract(String key, String connectionString) {

        Matcher matcher = Pattern.compile("^.+" + key + "=([^:;]+).*$").matcher(connectionString);

        if (matcher.find()) {

            return BooleanUtils.toBoolean(matcher.group(1));
        }

        return false;
    }

    @Override
    public String toString() {

        return String.format("%s[address = %s, channel = %s, authenticate = %s, encrypt = %s, master = %s, %s]", this.getClass().getName(), address, channel,
                authenticate, encrypt, master, super.toString());
    }
}
