package io.unifycom.event;

import io.unifycom.Envelope;

public class DatagramEvent<A> extends Envelope<ChannelEvent, A> implements ChannelEvent {

    public DatagramEvent(ChannelEvent content, A recipient) {

        super(content, recipient);
    }

    public DatagramEvent(ChannelEvent content, A recipient, A sender) {

        super(content, recipient, sender);
    }
}
