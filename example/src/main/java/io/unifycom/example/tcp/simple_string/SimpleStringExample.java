package io.unifycom.example.tcp.simple_string;

import io.unifycom.dispatch.DefaultChannelDispatcher;
import io.unifycom.example.tcp.simple_string.event.codec.MyMessageToEventDecoder;
import io.unifycom.example.tcp.simple_string.event.codec.MyResultToMessageEncoder;
import io.unifycom.example.tcp.simple_string.handler.ConnectedEventHandler;
import io.unifycom.example.tcp.simple_string.handler.DisconnectedEventHandler;
import io.unifycom.example.tcp.simple_string.handler.HelloEventHandler;
import io.unifycom.example.tcp.simple_string.handler.IdleEventHandler;
import io.unifycom.example.tcp.simple_string.interceptor.GlobalEventHandlerInterceptor;
import io.unifycom.example.tcp.simple_string.interceptor.HelloEventHandlerInterceptor;
import io.unifycom.netty.codec.DelimiterBasedStringDecoder;
import io.unifycom.netty.codec.StringToByteEncoder;
import io.unifycom.tcp.client.TcpChannel;
import io.unifycom.tcp.client.TcpChannelConfig;


import java.nio.charset.StandardCharsets;

public class SimpleStringExample {

    /*
     * 简单固定分隔符字符串切割报文帧，使用内置的DelimiterBasedStringDecoder、StringToByteEncoder
     */
    public static void main(String[] args) throws InterruptedException {

        StringToByteEncoder encoder = new StringToByteEncoder(StandardCharsets.UTF_8);
        DelimiterBasedStringDecoder decoder = new DelimiterBasedStringDecoder(1000, "/", StandardCharsets.UTF_8);

        MyMessageToEventDecoder eventDecoder = new MyMessageToEventDecoder();
        MyResultToMessageEncoder resultEncoder = new MyResultToMessageEncoder();
        DefaultChannelDispatcher dispatcher = new DefaultChannelDispatcher(eventDecoder, resultEncoder);

        TcpChannel channel = new TcpChannel(new TcpChannelConfig("localhost:8080"), decoder, encoder, dispatcher);

        channel.addLast(new HelloEventHandler(), new ConnectedEventHandler(), new DisconnectedEventHandler(), new IdleEventHandler());
        channel.addLast(new HelloEventHandlerInterceptor(), new GlobalEventHandlerInterceptor());

        channel.connect().blockUntilConnected();
        Thread.sleep(60 * 1000);
    }
}
