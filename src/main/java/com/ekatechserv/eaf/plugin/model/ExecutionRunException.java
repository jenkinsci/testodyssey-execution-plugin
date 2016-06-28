package com.ekatechserv.eaf.plugin.model;

public class ExecutionRunException extends Exception{

    public ExecutionRunException() {
    }

    public ExecutionRunException(String message) {
        super(message);
    }

    public ExecutionRunException(String message, Throwable cause) {
        super(message, cause);
    }
}
