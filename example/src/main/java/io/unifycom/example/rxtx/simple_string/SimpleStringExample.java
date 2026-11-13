package io.unifycom.example.rxtx.simple_string;


import io.unifycom.dispatch.DefaultDispatcher;
import io.unifycom.example.rxtx.simple_string.event.codec.GreetingEventDecoder;
import io.unifycom.example.rxtx.simple_string.event.codec.GreetingResultEncoder;
import io.unifycom.example.tcp.simple_string.interceptor.GlobalEventHandlerInterceptor;
import io.unifycom.example.tcp.simple_string.interceptor.GreetingEventHandlerInterceptor;
import io.unifycom.rxtx.RxtxChannel;
import io.unifycom.rxtx.RxtxChannelConfig;
import io.unifycom.netty.codec.DelimiterBasedStringDecoder;
import io.unifycom.netty.codec.StringToByteEncoder;
import purejavacomm.CommPortIdentifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

public class SimpleStringExample {

    public static void main(String[] args) throws InterruptedException, IOException {

        Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier portId = portList.nextElement();
            System.out.println("PortType=" + portId.getPortType() + ",Name=" + portId.getName());
        }

        StringToByteEncoder encoder = new StringToByteEncoder(StandardCharsets.UTF_8);
        DelimiterBasedStringDecoder decoder = new DelimiterBasedStringDecoder(1000, "/", StandardCharsets.UTF_8);

        GreetingEventDecoder eventDecoder = new GreetingEventDecoder();
        GreetingResultEncoder resultEncoder = new GreetingResultEncoder();
        DefaultDispatcher dispatcher = new DefaultDispatcher(eventDecoder, resultEncoder);

        RxtxChannelConfig config = new RxtxChannelConfig("COM10");
        config.setBaudrate(9600);

        RxtxChannel channel = new RxtxChannel(config, decoder, encoder, dispatcher);

        channel.addLast(new GreetingEventHandler(), new ConnectedEventHandler(), new DisconnectedEventHandler(), new IdleEventHandler());
        channel.addLast(new GreetingEventHandlerInterceptor(), new GlobalEventHandlerInterceptor());

        channel.connect().blockUntilConnected();
        Thread.sleep(60 * 1000);
    }
}
