package io.unifycom.udp.server;

import io.netty.channel.DefaultAddressedEnvelope;
import io.unifycom.Envelope;
import io.unifycom.netty.server.NettySessionChannel;
import java.net.SocketAddress;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpSessionChannel extends NettySessionChannel {

    private static final Logger logger = LoggerFactory.getLogger(UdpSessionChannel.class);

    private final SocketAddress remoteAddress;

    public UdpSessionChannel(io.netty.channel.Channel datagramChannel, SocketAddress remoteAddress) {

        super(datagramChannel);
        this.remoteAddress = remoteAddress;
    }

    public SocketAddress getRemoteAddress() {

        return this.remoteAddress;
    }

    @Override
    protected SocketAddress keySocketAddress() {

        return this.remoteAddress;
    }

    @Override
    protected Object wrapOutbound(Object out) {

        if (out instanceof Envelope) {

            Envelope envelope = (Envelope)out;
            SocketAddress receiver = envelope.getReceiver() != null ? (SocketAddress)envelope.getReceiver() : remoteAddress;

            return new DefaultAddressedEnvelope<>(envelope.getContent(), receiver, remoteAddress);
        }

        return new DefaultAddressedEnvelope<>(out, remoteAddress);
    }

    @Override
    public synchronized void close() {

        // Just detach the session, the underlying datagram channel is shared by all sessions,
        // and it will be closed by the UdpServerChannel when shutting down.
        logger.info("{} has been closed.", getName());
    }
}
