package io.unifycom.event;

public class ConnectedEvent extends AbstractConnectedEvent {

    public ConnectedEvent(String channelId, String remoteAddress){

        setChannelId(channelId);
        setRemoteAddress(remoteAddress);
    }

    @Override
    public String toString() {

        return String.format("Connected event from [%s].", getRemoteAddress());
    }
}
