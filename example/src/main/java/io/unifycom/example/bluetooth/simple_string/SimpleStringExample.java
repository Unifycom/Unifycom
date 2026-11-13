package io.unifycom.example.bluetooth.simple_string;


import io.unifycom.bluetooth.client.BluetoothChannel;
import io.unifycom.bluetooth.client.BluetoothChannelConfig;
import io.unifycom.dispatch.DefaultDispatcher;
import io.unifycom.example.bluetooth.simple_string.event.codec.GreetingEventDecoder;
import io.unifycom.example.bluetooth.simple_string.event.codec.GreetingResultEncoder;
import io.unifycom.example.bluetooth.simple_string.handler.ConnectedEventHandler;
import io.unifycom.example.bluetooth.simple_string.handler.DisconnectedEventHandler;
import io.unifycom.example.bluetooth.simple_string.handler.GreetingEventHandler;
import io.unifycom.example.bluetooth.simple_string.handler.IdleEventHandler;
import io.unifycom.example.tcp.simple_string.interceptor.GlobalEventHandlerInterceptor;
import io.unifycom.example.tcp.simple_string.interceptor.GreetingEventHandlerInterceptor;
import io.unifycom.netty.codec.DelimiterBasedStringDecoder;
import io.unifycom.netty.codec.StringToByteEncoder;
import org.apache.commons.io.Charsets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

public class SimpleStringExample {

    public static void main(String[] args) throws InterruptedException, IOException, ExecutionException {

        StringToByteEncoder encoder = new StringToByteEncoder(Charsets.toCharset("GB2312"));
        DelimiterBasedStringDecoder decoder = new DelimiterBasedStringDecoder(1000, "/", StandardCharsets.UTF_8);

        GreetingEventDecoder eventDecoder = new GreetingEventDecoder();
        GreetingResultEncoder resultEncoder = new GreetingResultEncoder();
        DefaultDispatcher dispatcher = new DefaultDispatcher(eventDecoder, resultEncoder);

        BluetoothChannelConfig config = new BluetoothChannelConfig("btspp://000878439229:1;authenticate=false;encrypt=false;master=false");

        BluetoothChannel channel = new BluetoothChannel(config, decoder, encoder, dispatcher);

        channel.addLast(new GreetingEventHandler(), new ConnectedEventHandler(), new DisconnectedEventHandler(), new IdleEventHandler());
        channel.addLast(new GreetingEventHandlerInterceptor(), new GlobalEventHandlerInterceptor());

        channel.connect().blockUntilConnected();

        String str = "! 0 200 200 210 1\r\n";
        str += "TEXT 4 0 30 40 Hello World\r\n";
        str += "PRINT\r\n";

        System.out.println(str);
        channel.send(str);

        Thread.sleep(60 * 1000);
    }
}
