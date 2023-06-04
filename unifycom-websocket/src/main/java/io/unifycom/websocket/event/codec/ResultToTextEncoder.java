package io.unifycom.websocket.event.codec;

import com.alibaba.fastjson2.JSON;
import io.unifycom.event.codec.ResultToMessageEncoder;
import io.unifycom.event.result.ChannelEventResult;

public class ResultToTextEncoder implements ResultToMessageEncoder<String> {

    @Override
    public String encode(ChannelEventResult eventResult) {

        return JSON.toJSONString(eventResult);
    }
}
