package io.unifycom.udp.server;

import io.unifycom.Channel;
import io.unifycom.Envelope;
import io.netty.channel.DefaultAddressedEnvelope;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpSessionChannel extends io.unifycom.netty.server.NettySessionChannel {

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
    public Future<Void> send(Object out) throws IOException {

        if (out instanceof Envelope) {

            Envelope envelope = (Envelope)out;
            SocketAddress receiver = envelope.getReceiver() != null ? (SocketAddress)envelope.getReceiver() : remoteAddress;

            out = new DefaultAddressedEnvelope<>(envelope.getContent(), receiver, remoteAddress);

        } else {

            out = new DefaultAddressedEnvelope<>(out, remoteAddress);
        }

        return super.send(out);
    }

    @Override
    public synchronized void close() {

        // Just detach the session, the underlying datagram channel is shared by all sessions,
        // and it will be closed by the UdpServerChannel when shutting down.
        logger.info("{} has been closed.", getName());
    }
}
