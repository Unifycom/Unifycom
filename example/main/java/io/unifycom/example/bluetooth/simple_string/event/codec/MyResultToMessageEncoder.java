package io.unifycom.example.bluetooth.simple_string.event.codec;

import io.unifycom.event.codec.ResultToMessageEncoder;
import io.unifycom.event.result.ChannelEventResult;

public class MyResultToMessageEncoder implements ResultToMessageEncoder<String> {

    @Override
    public String encode(ChannelEventResult eventResult) {

        return eventResult.toString();
    }
}
