package com.mindex.challenge.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CompensationNotFoundException extends RuntimeException {
    public CompensationNotFoundException(String message) { super(message); }
}