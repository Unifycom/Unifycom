

package io.unifycom.example.tcp.binary_frame;

import io.unifycom.dispatch.AbstractQueuableChannelDispatcher;
import io.unifycom.event.codec.MessageToEventDecoder;
import io.unifycom.event.codec.ResultToMessageEncoder;
import io.unifycom.example.tcp.binary_frame.protocol.InboundMessage;

public class MyQueuableChannelDispatcher extends AbstractQueuableChannelDispatcher {

    protected MyQueuableChannelDispatcher(MessageToEventDecoder messageToEventDecoder, ResultToMessageEncoder resultToMessageEncoder) {

        super(messageToEventDecoder, resultToMessageEncoder, 10, 100);
    }

    @Override
    public int groupBy(Object in) {

        if (in instanceof InboundMessage) {


            /*
             * 收到的同一个指令类型报文进相同队列，确保处理有序
             */
            return ((InboundMessage)in).getInstruction();
        }

        return 0;
    }
}
