package com.github.ant.function;

public class FatalExitError extends Error {

    private final static long serialVersionUID = 1L;

    private final int statusCode;

    public FatalExitError(int statusCode) {
        if (statusCode == 0)
            throw new IllegalArgumentException("statusCode must not be 0");
        this.statusCode = statusCode;
    }

    public FatalExitError() {
        this(1);
    }

    public int statusCode() {
        return statusCode;
    }
}