package com.joel.br.FlightMatrix.exceptions;

import org.springframework.http.HttpStatus;

public class IntegracaoAPIException extends RuntimeException {
    public IntegracaoAPIException(String message) {

    }
    public IntegracaoAPIException(String s, Throwable e) {
    }
    public IntegracaoAPIException(String message, HttpStatus httpStatus) {

    }
}
