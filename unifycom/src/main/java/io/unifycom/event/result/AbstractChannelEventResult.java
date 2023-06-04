package io.unifycom.event.result;


import io.unifycom.event.ChannelEvent;

public abstract class AbstractChannelEventResult implements ChannelEventResult {

    private ChannelEvent source;

    @Override
    public ChannelEvent getSource() {

        return source;
    }

    @Override
    public void setSource(ChannelEvent source) {

        this.source = source;
    }
}
