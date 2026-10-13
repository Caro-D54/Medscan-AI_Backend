package com.medscan.app_med.service;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Credenciales inválidas");
    }
}
