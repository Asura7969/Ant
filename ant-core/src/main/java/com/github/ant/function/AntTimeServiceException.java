package com.github.ant.function;

public class AntTimeServiceException extends RuntimeException {
    private static final long serialVersionUID = 2L;

    public AntTimeServiceException() {
        super();
    }

    public AntTimeServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public AntTimeServiceException(String message) {
        super(message);
    }

    public AntTimeServiceException(Throwable cause) {
        super(cause);
    }
}
