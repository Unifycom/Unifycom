package io.unifycom.netty.codec;

import io.netty.channel.ChannelPipeline;

/**
 * The utilities to assemble the unifycom codecs into a netty {@link ChannelPipeline}.
 */
public final class NettyCodecs {

    private NettyCodecs() {

    }

    /**
     * Adds the byte stream codecs (MessageToByteEncoder + ByteToMessageDecoder + optional MessageToMessageDecoder)
     * of the unifycom codec SPI to the pipeline, it is used by the byte stream transports (tcp/bluetooth/rxtx).
     */
    public static void addByteCodecHandlers(ChannelPipeline pipeline, NettyChannelEncoder<?> channelEncoder,
                                            NettyChannelDecoder channelDecoder) {

        if (channelEncoder == null || channelDecoder == null) {
            return;
        }

        pipeline.addLast(channelEncoder.getMessageToByteEncoder()).addLast(channelDecoder.getByteToMessageDecoder());

        if (channelDecoder.getMessageToMessageDecoder() != null) {
            pipeline.addLast(channelDecoder.getMessageToMessageDecoder());
        }
    }

    /**
     * Adds the message codecs (MessageToMessageDecoder + MessageToMessageEncoder)
     * of the unifycom codec SPI to the pipeline, it is used by the datagram transports (udp).
     */
    public static void addMessageCodecHandlers(ChannelPipeline pipeline, NettyChannelDecoder channelDecoder,
                                               NettyChannelEncoder<?> channelEncoder) {

        if (channelEncoder == null || channelDecoder == null) {
            return;
        }

        pipeline.addLast(channelDecoder.getMessageToMessageDecoder()).addLast(channelEncoder.getMessageToMessageEncoder());
    }
}
