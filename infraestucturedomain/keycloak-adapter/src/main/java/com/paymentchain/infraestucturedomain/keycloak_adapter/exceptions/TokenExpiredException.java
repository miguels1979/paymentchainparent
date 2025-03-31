package com.paymentchain.infraestucturedomain.keycloak_adapter.exceptions;

public class TokenExpiredException extends RuntimeException{

    public TokenExpiredException(String message) {
        super(message);
    }
}
