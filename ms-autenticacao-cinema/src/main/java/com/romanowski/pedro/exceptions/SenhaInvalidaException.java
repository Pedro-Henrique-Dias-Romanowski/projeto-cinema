package com.romanowski.pedro.exceptions;

public class SenhaInvalidaException extends SecurityException {
    public SenhaInvalidaException(String message) {
        super(message);
    }
}
