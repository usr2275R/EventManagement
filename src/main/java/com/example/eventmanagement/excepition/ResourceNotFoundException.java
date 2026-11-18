package com.example.eventmanagement.excepition;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String entity,Long id){
        super(entity +" not found with id" +
                " "+id);
    }
}
