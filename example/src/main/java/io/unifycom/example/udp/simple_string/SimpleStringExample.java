package io.unifycom.example.udp.simple_string;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import io.unifycom.dispatch.DefaultDispatcher;
import io.unifycom.example.udp.simple_string.event.codec.GreetingEventDecoder;
import io.unifycom.example.udp.simple_string.event.codec.GreetingResultEncoder;
import io.unifycom.example.udp.simple_string.handler.ConnectedEventHandler;
import io.unifycom.example.udp.simple_string.handler.DisconnectedEventHandler;
import io.unifycom.example.udp.simple_string.handler.GreetingEventHandler;
import io.unifycom.example.udp.simple_string.handler.IdleEventHandler;
import io.unifycom.example.udp.simple_string.interceptor.GlobalEventHandlerInterceptor;
import io.unifycom.example.udp.simple_string.interceptor.GreetingEventHandlerInterceptor;
import io.unifycom.netty.codec.AbstractNettyDatagramChannelDecoder;
import io.unifycom.netty.codec.AbstractNettyDatagramChannelEncoder;
import io.unifycom.netty.codec.NettyChannelDecoder;
import io.unifycom.netty.codec.NettyChannelEncoder;
import io.unifycom.udp.client.UdpChannel;
import io.unifycom.udp.client.UdpChannelConfig;

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

        GreetingEventDecoder eventDecoder = new GreetingEventDecoder();
        GreetingResultEncoder resultEncoder = new GreetingResultEncoder();
        DefaultDispatcher dispatcher = new DefaultDispatcher(eventDecoder, resultEncoder);

        UdpChannel channel = new UdpChannel(new UdpChannelConfig("localhost:9080"), decoder, encoder, dispatcher);

        channel.addLast(new GreetingEventHandler(), new ConnectedEventHandler(), new DisconnectedEventHandler(), new IdleEventHandler());
        channel.addLast(new GreetingEventHandlerInterceptor(), new GlobalEventHandlerInterceptor());

        channel.connect().blockUntilConnected();
        Thread.sleep(60 * 1000);
    }
}
