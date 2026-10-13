package com.medscan.app_med.service;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("El correo " + email + " ya está registrado");
    }
}
