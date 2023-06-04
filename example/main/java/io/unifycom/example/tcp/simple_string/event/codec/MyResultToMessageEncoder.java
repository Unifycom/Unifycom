package io.unifycom.example.tcp.simple_string.event.codec;

import io.unifycom.event.codec.ResultToMessageEncoder;
import io.unifycom.event.result.ChannelEventResult;

public class MyResultToMessageEncoder implements ResultToMessageEncoder<String> {

    @Override
    public String encode(ChannelEventResult eventResult) {

        return eventResult.toString();
    }
}
