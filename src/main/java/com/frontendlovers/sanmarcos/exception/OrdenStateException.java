package com.frontendlovers.sanmarcos.exception;

// Se lanza ante una transición de estado inválida o cancelación no permitida
public class OrdenStateException extends RuntimeException {
    public OrdenStateException(String message) {
        super(message);
    }
}
