package io.unifycom;

public class Envelope<M, A> {

    private M content;
    private A sender;
    private A recipient;

    public Envelope(M content, A recipient) {

        this(content, recipient, null);
    }

    public Envelope(M content, A recipient, A sender) {

        this.content = content;
        this.recipient = recipient;
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

    public A getRecipient() {

        return recipient;
    }

    public void setRecipient(A recipient) {

        this.recipient = recipient;
    }

    @Override
    public String toString() {

        return String.format("[%s, SENDER = %s, RECIPIENT = %s]", content, sender, recipient);
    }
}
