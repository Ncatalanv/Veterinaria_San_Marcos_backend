package com.frontendlovers.sanmarcos.exception;

// Se lanza cuando no hay stock suficiente para completar una orden
public class StockInsuficienteException extends RuntimeException {
    public StockInsuficienteException(String message) {
        super(message);
    }
}
