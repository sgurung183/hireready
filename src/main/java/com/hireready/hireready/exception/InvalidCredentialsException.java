package com.hireready.hireready.exception;

public class InvalidCredentialsException extends  RuntimeException{
    public InvalidCredentialsException(String message){
        super(message);
    }
}
