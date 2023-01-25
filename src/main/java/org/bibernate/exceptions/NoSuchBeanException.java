package org.bibernate.exceptions;

public class NoSuchBeanException extends RuntimeException{
    public NoSuchBeanException(String message) {
        super(message);
    }
}
