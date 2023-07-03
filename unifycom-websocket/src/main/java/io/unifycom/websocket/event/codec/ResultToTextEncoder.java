package io.unifycom.websocket.event.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.unifycom.event.codec.ResultToMessageEncoder;
import io.unifycom.event.result.ChannelEventResult;

public class ResultToTextEncoder implements ResultToMessageEncoder<String> {

    private ObjectMapper JSON = new ObjectMapper();

    @Override
    public String encode(ChannelEventResult eventResult) throws Exception {

        return JSON.writeValueAsString(eventResult);
    }
}
