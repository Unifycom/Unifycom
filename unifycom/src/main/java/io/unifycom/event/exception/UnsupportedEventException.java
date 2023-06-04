package io.unifycom.event.exception;

public class UnsupportedEventException extends Exception {

    private static final long serialVersionUID = 1L;

    public UnsupportedEventException() {

        super();
    }

    public UnsupportedEventException(String message) {

        super(message);
    }
}