package io.unifycom.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InboundProxy2Decoder extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(InboundProxy2Decoder.class);

    private static final AttributeKey<String> X_REAL_IP = AttributeKey.valueOf("X-Real-IP");
    private static final ByteBuf HEAD = Unpooled.wrappedBuffer(new byte[]{0x50, 0x52, 0x4f, 0x58, 0x59, 0x20, 0x54, 0x43, 0x50, 0x34, 0x20});

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (!(msg instanceof ByteBuf)) {

            ctx.fireChannelRead(msg);
            return;
        }

        ByteBuf in = (ByteBuf)msg;

        if (in.writerIndex() <= HEAD.writerIndex()) {

            ctx.fireChannelRead(in);
            return;
        }

        if (in.slice(0, HEAD.writerIndex()).compareTo(HEAD) != 0) {
            ctx.fireChannelRead(in);
            return;
        }
        String pp = in.readCharSequence(in.writerIndex(), StandardCharsets.UTF_8).toString();
        String[] slices = StringUtils.split(pp, StringUtils.SPACE, 6);

        if (ArrayUtils.getLength(slices) == 6) {

            String realIp = slices[2];
            ctx.channel().attr(X_REAL_IP).set(realIp);

            if (logger.isDebugEnabled()) {

                logger.debug("{} {} of {}.", X_REAL_IP.name(), realIp, ctx.channel().remoteAddress());
            }
        }

        ReferenceCountUtil.release(in);
    }
}
