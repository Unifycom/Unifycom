package io.unifycom.example.udp.simple_string;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import io.unifycom.dispatch.DefaultChannelDispatcher;
import io.unifycom.example.udp.simple_string.event.codec.MyMessageToEventDecoder;
import io.unifycom.example.udp.simple_string.event.codec.MyResultToMessageEncoder;
import io.unifycom.example.udp.simple_string.handler.ConnectedEventHandler;
import io.unifycom.example.udp.simple_string.handler.DisconnectedEventHandler;
import io.unifycom.example.udp.simple_string.handler.HelloEventHandler;
import io.unifycom.example.udp.simple_string.handler.IdleEventHandler;
import io.unifycom.example.udp.simple_string.interceptor.GlobalEventHandlerInterceptor;
import io.unifycom.example.udp.simple_string.interceptor.HelloEventHandlerInterceptor;
import io.unifycom.netty.codec.AbstractNettyDatagramChannelDecoder;
import io.unifycom.netty.codec.AbstractNettyDatagramChannelEncoder;
import io.unifycom.netty.codec.NettyChannelDecoder;
import io.unifycom.netty.codec.NettyChannelEncoder;
import io.unifycom.udp.UdpChannel;
import io.unifycom.udp.UdpChannelConfig;
import java.nio.charset.StandardCharsets;

public class SimpleStringExample {


    public static void main(String[] args) throws InterruptedException {

        NettyChannelEncoder encoder = new AbstractNettyDatagramChannelEncoder<String>() {

            @Override
            public ByteBuf encode(String out) {

                return Unpooled.copiedBuffer(out, CharsetUtil.UTF_8);
            }
        };

        NettyChannelDecoder decoder = new AbstractNettyDatagramChannelDecoder<String>() {

            public String decode(ByteBuf in) {

                return in.toString(StandardCharsets.UTF_8);
            }
        };

        MyMessageToEventDecoder eventDecoder = new MyMessageToEventDecoder();
        MyResultToMessageEncoder resultEncoder = new MyResultToMessageEncoder();
        DefaultChannelDispatcher dispatcher = new DefaultChannelDispatcher(eventDecoder, resultEncoder);

        UdpChannel channel = new UdpChannel(new UdpChannelConfig("localhost:9080"), decoder, encoder, dispatcher);

        channel.addLast(new HelloEventHandler(), new ConnectedEventHandler(), new DisconnectedEventHandler(), new IdleEventHandler());
        channel.addLast(new HelloEventHandlerInterceptor(), new GlobalEventHandlerInterceptor());

        channel.connect().blockUntilConnected();
        Thread.sleep(60 * 1000);
    }
}
