package io.unifycom;

public interface Pong {

    /**
     * @param out The response for heartbeat ping message from remote.
     */
    void pong(Object out);
}
