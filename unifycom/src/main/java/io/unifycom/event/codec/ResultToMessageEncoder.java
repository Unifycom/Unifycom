package io.unifycom.event.codec;

import io.unifycom.event.result.ChannelEventResult;

public interface ResultToMessageEncoder<T> {

    T encode(ChannelEventResult eventResult);
}
