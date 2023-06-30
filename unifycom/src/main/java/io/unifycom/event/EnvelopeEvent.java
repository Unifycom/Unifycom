package io.unifycom.event;

import io.unifycom.Envelope;

public class EnvelopeEvent<A> extends Envelope<ChannelEvent, A> implements ChannelEvent {

    public EnvelopeEvent(ChannelEvent content, A recipient) {

        super(content, recipient);
    }

    public EnvelopeEvent(ChannelEvent content, A recipient, A sender) {

        super(content, recipient, sender);
    }
}
