package com.romanowski.pedro.exceptions;

public class EmailExistenteException extends SecurityException {
    public EmailExistenteException(String message) {
        super(message);
    }
}
