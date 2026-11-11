package com.example.eventmanagement.excepition;

public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message){
        super(message);
    }
}
