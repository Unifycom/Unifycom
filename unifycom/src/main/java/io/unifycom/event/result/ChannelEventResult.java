package io.unifycom.event.result;

import io.unifycom.event.ChannelEvent;

public interface ChannelEventResult {

    NoReplyEventResult NoReply = new NoReplyEventResult();

    ChannelEvent getSource();

    void setSource(ChannelEvent source);
}
