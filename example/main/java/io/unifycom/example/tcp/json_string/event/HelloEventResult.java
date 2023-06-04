package io.unifycom.example.tcp.json_string.event;

import io.unifycom.event.result.AbstractChannelEventResult;

public class HelloEventResult extends AbstractChannelEventResult {

    private String say;

    public HelloEventResult(String say) {

        this.say = say;
    }

    public String getSay() {

        return say;
    }

    public void setSay(String say) {

        this.say = say;
    }
}
