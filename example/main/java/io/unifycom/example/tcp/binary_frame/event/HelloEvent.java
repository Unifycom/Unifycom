package io.unifycom.example.tcp.binary_frame.event;

import io.unifycom.event.ChannelEvent;

public class HelloEvent implements ChannelEvent {

    private String name;
    private String sex;

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getSex() {

        return sex;
    }

    public void setSex(String sex) {

        this.sex = sex;
    }
}
