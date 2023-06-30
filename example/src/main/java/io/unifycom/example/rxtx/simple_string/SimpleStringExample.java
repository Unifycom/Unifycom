package io.unifycom.example.rxtx.simple_string;


import io.unifycom.dispatch.DefaultChannelDispatcher;
import io.unifycom.example.rxtx.simple_string.event.codec.MyMessageToEventDecoder;
import io.unifycom.example.rxtx.simple_string.event.codec.MyResultToMessageEncoder;
import io.unifycom.example.tcp.simple_string.interceptor.GlobalEventHandlerInterceptor;
import io.unifycom.example.tcp.simple_string.interceptor.HelloEventHandlerInterceptor;
import io.unifycom.netty.codec.DelimiterBasedStringDecoder;
import io.unifycom.netty.codec.StringToByteEncoder;
import io.unifycom.rxtx.RxtxChannel;
import io.unifycom.rxtx.RxtxChannelConfig;
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

        MyMessageToEventDecoder eventDecoder = new MyMessageToEventDecoder();
        MyResultToMessageEncoder resultEncoder = new MyResultToMessageEncoder();
        DefaultChannelDispatcher dispatcher = new DefaultChannelDispatcher(eventDecoder, resultEncoder);

        RxtxChannelConfig config = new RxtxChannelConfig("COM10");
        config.setBaudrate(9600);

        RxtxChannel channel = new RxtxChannel(config, decoder, encoder, dispatcher);

        channel.addLast(new HelloEventHandler(), new ConnectedEventHandler(), new DisconnectedEventHandler(), new IdleEventHandler());
        channel.addLast(new HelloEventHandlerInterceptor(), new GlobalEventHandlerInterceptor());

        channel.connect().blockUntilConnected();
        Thread.sleep(60 * 1000);
    }
}
