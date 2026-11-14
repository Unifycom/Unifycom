package io.unifycom.bluetooth.server;

import io.unifycom.netty.AbstractSocketChannelConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BluetoothServerChannelConfig extends AbstractSocketChannelConfig {

    private static final String DEFAULT_PROTOCOL = "btspp";
    private static final String DEFAULT_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private static final String DEFAULT_NAME = "unifycom";

    private String protocol = DEFAULT_PROTOCOL;
    private String uuid = DEFAULT_UUID;
    private String name = DEFAULT_NAME;
    private boolean authenticate = false;
    private boolean encrypt = false;
    private boolean master = false;

    public BluetoothServerChannelConfig() {

    }

    public BluetoothServerChannelConfig(String connectionString) {

        this.setConnectionString(connectionString);
    }

    public String getProtocol() {

        return protocol;
    }

    public void setProtocol(String protocol) {

        this.protocol = protocol;
    }

    public String getUuid() {

        return uuid;
    }

    public void setUuid(String uuid) {

        this.uuid = uuid;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
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

    @Override
    public String getConnectionString() {

        if (StringUtils.isNotBlank(super.getConnectionString())) {

            return super.getConnectionString();
        }

        return String.format("%s://localhost:%s;name=%s;authenticate=%s;encrypt=%s;master=%s", protocol, uuid, name, authenticate, encrypt, master);
    }

    @Override
    public void setConnectionString(String connectionString) {

        Matcher matcher1 = Pattern.compile("^([^:]+)://localhost:([^;:]+).*$").matcher(connectionString);

        if (!matcher1.find()) {

            throw new IllegalArgumentException(String.format("Illegal connection string %s.", connectionString));
        }

        this.protocol = matcher1.group(1);
        this.uuid = matcher1.group(2);

        Matcher matcher2 = Pattern.compile("^.+name=([^:;]+).*$").matcher(connectionString);

        if (matcher2.find()) {

            this.name = matcher2.group(1);
        }

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

        return String.format("%s[protocol = %s, uuid = %s, name = %s, authenticate = %s, encrypt = %s, master = %s, %s]",
                this.getClass().getName(), protocol, uuid, name, authenticate, encrypt, master, super.toString());
    }
}
