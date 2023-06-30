package io.unifycom.example.bluetooth.simple_string.event;

import io.unifycom.event.ChannelEvent;

public class HelloEvent implements ChannelEvent {

    private String str1;

    public String getStr1() {

        return str1;
    }

    public void setStr1(String str1) {

        this.str1 = str1;
    }
}
