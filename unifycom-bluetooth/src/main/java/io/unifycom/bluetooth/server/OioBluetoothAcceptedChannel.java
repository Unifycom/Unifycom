package io.unifycom.bluetooth.server;

import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.oio.OioByteStreamChannel;
import io.unifycom.netty.channel.bluetooth.BluetoothDeviceAddress;
import java.io.IOException;
import java.net.SocketAddress;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.StreamConnection;

/**
 * The server side bluetooth channel, wraps the accepted {@link StreamConnection} into a netty OIO channel.
 */
@SuppressWarnings("deprecation")
public class OioBluetoothAcceptedChannel extends OioByteStreamChannel {

    private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);

    private final StreamConnection connection;
    private final ChannelConfig config;
    private volatile boolean open = true;

    private SocketAddress localAddress;
    private SocketAddress remoteAddress;

    public OioBluetoothAcceptedChannel(StreamConnection connection) {

        super(null);

        this.connection = connection;
        this.config = new DefaultChannelConfig(this);
    }

    public StreamConnection connection() {

        return connection;
    }

    public void activate() throws IOException {

        activate(connection.openInputStream(), connection.openOutputStream());
    }

    @Override
    public ChannelMetadata metadata() {

        return METADATA;
    }

    @Override
    public ChannelConfig config() {

        return config;
    }

    @Override
    public boolean isOpen() {

        return open;
    }

    @Override
    public boolean isActive() {

        return open && super.isActive();
    }

    @Override
    protected SocketAddress localAddress0() {

        if (localAddress == null) {

            try {
                localAddress = new BluetoothDeviceAddress(LocalDevice.getLocalDevice().getBluetoothAddress());
            } catch (Exception e) {
                return null;
            }
        }

        return localAddress;
    }

    @Override
    protected SocketAddress remoteAddress0() {

        if (remoteAddress == null) {

            try {
                remoteAddress = new BluetoothDeviceAddress(RemoteDevice.getRemoteDevice(connection).getBluetoothAddress());
            } catch (Exception e) {
                return null;
            }
        }

        return remoteAddress;
    }

    @Override
    protected boolean isInputShutdown() {

        return !isOpen();
    }

    @Override
    protected ChannelFuture shutdownInput() {

        return newSucceededFuture();
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {

        throw new UnsupportedOperationException();
    }

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {

        throw new UnsupportedOperationException("The channel is accepted by server, this operation is unsupported on server side.");
    }

    @Override
    protected void doDisconnect() throws Exception {

        doClose();
    }

    @Override
    protected void doClose() throws Exception {

        open = false;
        connection.close();
    }
}
