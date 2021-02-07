package com.github.ant.function;

public class ExistsException extends RuntimeException {
    private static final long serialVersionUID = 2L;

    public ExistsException() {
        super();
    }

    public ExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExistsException(String message) {
        super(message);
    }

    public ExistsException(Throwable cause) {
        super(cause);
    }
}
