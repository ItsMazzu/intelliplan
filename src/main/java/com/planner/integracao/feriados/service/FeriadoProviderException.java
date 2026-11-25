package com.planner.integracao.feriados.service;

public class FeriadoProviderException extends RuntimeException {

    public FeriadoProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public FeriadoProviderException(String message) {
        super(message);
    }
}
