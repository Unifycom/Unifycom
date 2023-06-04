package io.unifycom.example.tcp.binary_frame;

import io.unifycom.example.tcp.binary_frame.codec.TcpChannelInboundDecoder;
import io.unifycom.example.tcp.binary_frame.codec.TcpChannelOutboundEncoder;
import io.unifycom.example.tcp.binary_frame.event.codec.MyMessageToEventDecoder;
import io.unifycom.example.tcp.binary_frame.event.codec.MyResultToMessageEncoder;
import io.unifycom.example.tcp.binary_frame.handler.ConnectedEventHandler;
import io.unifycom.example.tcp.binary_frame.handler.DisconnectedEventHandler;
import io.unifycom.example.tcp.binary_frame.handler.HelloEventHandler;
import io.unifycom.example.tcp.binary_frame.handler.IdleEventHandler;
import io.unifycom.tcp.client.TcpChannel;
import io.unifycom.tcp.client.TcpChannelConfig;


public class BinaryFrameExample {

    /*
     * Protocol format (实际生产中建议加上校验等字段):
     * +---STX--+---INS--+---LEN--+---VER--+---SEQ--+--TIMESTAMP--+---PAYLOAD---+---ETX--+
     * |  (1B)  |  (2B)  |  (2B)  |  (2B)  |  (8B)  |    (8B)     |   (FREE)    |  (2B)  |
     * +--------+--------+-------+---------+--------+-------------+-------------+--------+
     * LEN = LEN(VER) + LEN(SEQ) + LEN(TIMESTAMP) + LEN(PAYLOAD)
     *
     * e.g.
     * STX = 3A
     * INS = 100 (00 64)
     * LEN = 44 (00 0x2C)
     * VER = 01 (01)
     * SEQ = 99 (00 00 00 00 00 00 03 E7)
     * TIMESTAMP = 1675140205569 (00 00 01 86 06 23 0C 01)
     * PAYLOAY = {"name":"Jerry", "sex":"M"} (7B 22 6E 61 6D 65 22 3A 22 4A 65 72 72 79 22 2C 20 22 73 65 78 22 3A 22 4D 22 7D)
     * ETX = 0D 0A
     *
     * 最终报文16进制内容：
     * [3A 00 64 00 2C 01 00 00 00 00 00 00 03 E7 00 00 01 86 06 23 0C 01 7B 22 6E 61 6D 65 22 3A 22 4A 65 72 72 79 22 2C 20 22 73 65 78 22 3A 22 4D 22 7D 0D 0A]
     * */

    public static void main(String[] args) throws InterruptedException {

        TcpChannelOutboundEncoder encoder = new TcpChannelOutboundEncoder();
        TcpChannelInboundDecoder decoder = new TcpChannelInboundDecoder();

        MyMessageToEventDecoder eventDecoder = new MyMessageToEventDecoder();
        MyResultToMessageEncoder resultEncoder = new MyResultToMessageEncoder();

        // 使用自定义队列派发器
        MyQueuableChannelDispatcher dispatcher = new MyQueuableChannelDispatcher(eventDecoder, resultEncoder);

        TcpChannel channel = new TcpChannel(new TcpChannelConfig("localhost:8080"), decoder, encoder, dispatcher);
        /*
         * 如果需要服务器端，仅需要将TcpChannel更换为TcpServerChannel即可
         * TcpServerChannel channel = new TcpServerChannel(new TcpServerChannelConfig("8080"), decoder, encoder, dispatcher);
         */

        channel.addLast(new HelloEventHandler(), new ConnectedEventHandler(), new DisconnectedEventHandler(), new IdleEventHandler());

        channel.connect().blockUntilConnected();
        Thread.sleep(60 * 1000);
    }
}
