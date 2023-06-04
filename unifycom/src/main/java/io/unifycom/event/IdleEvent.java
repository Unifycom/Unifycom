package io.unifycom.event;

public class IdleEvent implements ChannelEvent {

    public static final IdleEvent ALL_IDLE_EVENT = new IdleEvent(Idle.ALL);
    public static final IdleEvent READER_IDLE_EVENT = new IdleEvent(Idle.READER);
    public static final IdleEvent WRITER_IDLE_EVENT = new IdleEvent(Idle.WRITER);

    private Idle idle;

    public IdleEvent(Idle idle) {

        this.idle = idle;
    }

    public Idle getIdle() {

        return this.idle;
    }

    @Override
    public String toString() {

        return String.format("%s IDLE EVENT.", idle);
    }
}
