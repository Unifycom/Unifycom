package io.unifycom.websocket.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.unifycom.dispatch.DefaultDispatcher;
import io.unifycom.netty.server.AbstractNettyChannelRegistry;
import io.unifycom.netty.server.AbstractNettyServerChannel;
import io.unifycom.websocket.event.codec.AbstractBinaryToEventDecoder;
import io.unifycom.websocket.event.codec.AbstractTextToEventDecoder;
import io.unifycom.websocket.event.codec.ResultToTextEncoder;
import io.unifycom.websocket.event.codec.WebsocketMessageToEventDecoder;

public class WebsocketServerChannel extends AbstractNettyServerChannel {

    private static final int MAX_HTTP_CONTENT_LENGTH = 65536;

    public WebsocketServerChannel(WebsocketServerChannelConfig config, AbstractTextToEventDecoder textToEventDecoder) {

        this(config, textToEventDecoder, null);
    }

    public WebsocketServerChannel(WebsocketServerChannelConfig config, AbstractTextToEventDecoder textToEventDecoder,
                                  AbstractBinaryToEventDecoder binaryToEventDecoder) {

        super(config, null, null, new AbstractNettyChannelRegistry());

        this.channelDispatcher = new DefaultDispatcher(new WebsocketMessageToEventDecoder(textToEventDecoder, binaryToEventDecoder),
                                                       new ResultToTextEncoder());
    }

    @Override
    protected void configureServerBootstrap(ServerBootstrap serverBootstrap) {

        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.TCP_NODELAY, true);
    }

    @Override
    protected void initChannel(io.netty.channel.Channel ch) {

        WebsocketServerChannelConfig config = (WebsocketServerChannelConfig)this.config;

        ch.pipeline().addLast(new HttpServerCodec()).addLast(new HttpObjectAggregator(MAX_HTTP_CONTENT_LENGTH))
            .addLast(new IdleStateHandler(config.getReaderIdleTimeSeconds(), config.getWriterIdleTimeSeconds(),
                                          config.getAllIdleTimeSeconds()))
            .addLast(new WebSocketServerProtocolHandler(config.getPath(), null, true))
            .addLast(new WebSocketFrameAggregator(config.getMaxFramePayloadLength()))
            .addLast(new WebsocketChannelInboundHandler(channelDispatcher, channelHolder));
    }
}
