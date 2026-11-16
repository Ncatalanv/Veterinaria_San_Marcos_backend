package com.frontendlovers.sanmarcos.exception;

// Se lanza ante una transición de estado inválida en una solicitud de servicio
public class SolicitudStateException extends RuntimeException {
    public SolicitudStateException(String message) {
        super(message);
    }
}
