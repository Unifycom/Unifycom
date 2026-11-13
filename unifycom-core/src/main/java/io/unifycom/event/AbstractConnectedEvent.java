package io.unifycom.event;

public class AbstractConnectedEvent implements ChannelEvent {

    private String channelId;
    private String remoteAddress;

    public String getChannelId() {

        return channelId;
    }

    public void setChannelId(String channelId) {

        this.channelId = channelId;
    }

    public String getRemoteAddress() {

        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {

        this.remoteAddress = remoteAddress;
    }
}
