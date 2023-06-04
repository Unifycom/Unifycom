package io.unifycom.websocket.server;

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.websocket;

import io.unifycom.AbstractServerChannel;
import io.unifycom.Channel;
import io.unifycom.dispatch.DefaultChannelDispatcher;
import io.unifycom.websocket.event.codec.AbstractTextToEventDecoder;
import io.unifycom.websocket.event.codec.ResultToTextEncoder;
import io.undertow.Undertow;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketServerChannel extends AbstractServerChannel {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketServerChannel.class);

    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private final String id = WebsocketServerChannel.class.getSimpleName() + "-" + COUNTER.getAndIncrement();

    private Undertow undertow;
    private WebsocketChannelGroup wsChannelGroup;

    private WebsocketServerChannelConfig config;
    private AbstractTextToEventDecoder textToEventDecoder;

    public WebsocketServerChannel(WebsocketServerChannelConfig config, AbstractTextToEventDecoder textToEventDecoder) {

        this.config = config;
        this.textToEventDecoder = textToEventDecoder;

        this.wsChannelGroup = new WebsocketChannelGroup();
        this.channelDispatcher = new DefaultChannelDispatcher(textToEventDecoder, new ResultToTextEncoder());

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    @Override
    public String getId() {

        return this.id;
    }

    @Override
    public synchronized AbstractServerChannel startup() {

        if (undertow != null) {

            logger.warn("{} is listening on {}, don't start-up it again.", getName(), config.getPort(), config.getPath());
            return this;
        }

        undertow = Undertow.builder().addHttpListener(config.getPort(), config.getHost())
            .setHandler(path().addPrefixPath(config.getPath(), websocket(new WebsocketSessionHandler(channelDispatcher, wsChannelGroup)))).build();

        undertow.start();

        logger.info("{} server is listening on {} ...... ", getId(), config.getConnectionString());

        return this;
    }

    @Override
    public void shutdown() {

        if (undertow != null) {

            undertow.stop();
            undertow = null;
            logger.info("{} on {} has been shutdown.", getName(), config.getPort());
        }
    }

    @Override
    public boolean isReady() {

        return undertow != null;
    }

    @Override
    public Future<Void> send(String channelName, Object out) throws IOException {

        Channel ch = getClient(channelName);

        if (ch == null) {

            logger.error("Not found any channel by name {}.", channelName);
            return null;
        }

        return ch.send(out);
    }

    @Override
    public Channel getClient(String channelName) {

        return wsChannelGroup.getByName(channelName);
    }
}
