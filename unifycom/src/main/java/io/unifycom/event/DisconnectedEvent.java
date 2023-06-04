package io.unifycom.event;

public class DisconnectedEvent extends AbstractConnectedEvent {

    public DisconnectedEvent(String channelId, String remoteAddress){

        setChannelId(channelId);
        setRemoteAddress(remoteAddress);
    }

    @Override
    public String toString() {

        return String.format("Disconnected event [%s].", getRemoteAddress());
    }
}
