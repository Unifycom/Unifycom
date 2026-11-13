package io.unifycom.example.tcp.json_string;

import io.unifycom.dispatch.DefaultDispatcher;
import io.unifycom.example.tcp.json_string.codec.DelimiterBasedJsonDecoder;
import io.unifycom.example.tcp.json_string.codec.JsonMessageToByteEncoder;
import io.unifycom.example.tcp.json_string.event.codec.GreetingResultEncoder;
import io.unifycom.example.tcp.json_string.handler.ConnectedEventHandler;
import io.unifycom.example.tcp.json_string.handler.DisconnectedEventHandler;
import io.unifycom.example.tcp.json_string.handler.GreetingEventHandler;
import io.unifycom.example.tcp.json_string.handler.IdleEventHandler;
import io.unifycom.example.tcp.json_string.event.codec.GreetingEventDecoder;
import io.unifycom.tcp.client.TcpChannel;
import io.unifycom.tcp.client.TcpChannelConfig;

import java.nio.charset.StandardCharsets;

public class JsonStringExample {

    /*
     * 固定分隔符字符串切割报文帧，切割后的JSON字符串报文帧通过MessageToMessageDecoder再次转换为Message对象
     */
    public static void main(String[] args) throws InterruptedException {

        JsonMessageToByteEncoder encoder = new JsonMessageToByteEncoder(StandardCharsets.UTF_8);
        DelimiterBasedJsonDecoder decoder = new DelimiterBasedJsonDecoder(1000, "/", StandardCharsets.UTF_8);

        GreetingEventDecoder eventDecoder = new GreetingEventDecoder();
        GreetingResultEncoder resultEncoder = new GreetingResultEncoder();
        DefaultDispatcher dispatcher = new DefaultDispatcher(eventDecoder, resultEncoder);

        TcpChannel channel = new TcpChannel(new TcpChannelConfig("localhost:8080"), decoder, encoder, dispatcher);

        channel.addLast(new GreetingEventHandler(), new ConnectedEventHandler(), new DisconnectedEventHandler(), new IdleEventHandler());

        channel.connect().blockUntilConnected();
        Thread.sleep(60 * 1000);
    }
}
