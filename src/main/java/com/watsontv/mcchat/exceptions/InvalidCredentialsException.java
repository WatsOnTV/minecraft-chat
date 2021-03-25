package com.watsontv.mcchat.exceptions;

public class InvalidCredentialsException extends Exception{
    public InvalidCredentialsException(String errorMessage) {
        super(errorMessage);
    }
}
