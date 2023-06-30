package io.unifycom.example.tcp.simple_string.event;

import io.unifycom.event.result.AbstractChannelEventResult;

public class HelloEventResult extends AbstractChannelEventResult {

    private String str1;

    public HelloEventResult(String str1) {

        this.str1 = str1;
    }


    public String getStr1() {

        return str1;
    }

    public void setStr1(String str1) {

        this.str1 = str1;
    }
}
