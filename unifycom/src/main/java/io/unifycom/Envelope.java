package io.unifycom;

public class Envelope<M, A> {

    private M content;
    private A sender;
    private A receiver;

    public Envelope(M content, A receiver) {

        this(content, receiver, null);
    }

    public Envelope(M content, A receiver, A sender) {

        this.content = content;
        this.receiver = receiver;
        this.sender = sender;
    }

    public M getContent() {

        return content;
    }

    public void setContent(M content) {

        this.content = content;
    }

    public A getSender() {

        return sender;
    }

    public void setSender(A sender) {

        this.sender = sender;
    }

    public A getReceiver() {

        return receiver;
    }

    public void setReceiver(A receiver) {

        this.receiver = receiver;
    }

    @Override
    public String toString() {

        return String.format("[%s, SENDER = %s, RECEIVER = %s]", content, sender, receiver);
    }
}
