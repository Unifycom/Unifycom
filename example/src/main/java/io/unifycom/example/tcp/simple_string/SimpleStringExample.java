package io.unifycom.example.tcp.simple_string;

import io.unifycom.dispatch.DefaultDispatcher;
import io.unifycom.example.tcp.simple_string.event.codec.GreetingEventDecoder;
import io.unifycom.example.tcp.simple_string.event.codec.GreetingResultEncoder;
import io.unifycom.example.tcp.simple_string.handler.ConnectedEventHandler;
import io.unifycom.example.tcp.simple_string.handler.DisconnectedEventHandler;
import io.unifycom.example.tcp.simple_string.handler.GreetingEventHandler;
import io.unifycom.example.tcp.simple_string.handler.IdleEventHandler;
import io.unifycom.example.tcp.simple_string.interceptor.GlobalEventHandlerInterceptor;
import io.unifycom.example.tcp.simple_string.interceptor.GreetingEventHandlerInterceptor;
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

        GreetingEventDecoder eventDecoder = new GreetingEventDecoder();
        GreetingResultEncoder resultEncoder = new GreetingResultEncoder();
        DefaultDispatcher dispatcher = new DefaultDispatcher(eventDecoder, resultEncoder);

        TcpChannel channel = new TcpChannel(new TcpChannelConfig("localhost:8080"), decoder, encoder, dispatcher);

        channel.addLast(new GreetingEventHandler(), new ConnectedEventHandler(), new DisconnectedEventHandler(), new IdleEventHandler());
        channel.addLast(new GreetingEventHandlerInterceptor(), new GlobalEventHandlerInterceptor());

        channel.connect().blockUntilConnected();
        Thread.sleep(60 * 1000);
    }
}
