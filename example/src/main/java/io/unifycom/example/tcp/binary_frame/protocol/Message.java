package io.unifycom.example.tcp.binary_frame.protocol;

public interface Message {

    int STX = 0x3A;
    int ETX = 0x0D0A;

    int LENGTH_OF_STX = 1;
    int LENGTH_OF_INS = 2;
    int LENGTH_OF_LEN = 2;
    int LENGTH_OF_VER = 1;
    int LENGTH_OF_SEQ = 8;
    int LENGTH_OF_TIMESTAMP = 8;
    int LENGTH_OF_ETX = 2;

    int MAX_LENGTH = 1024;
}
