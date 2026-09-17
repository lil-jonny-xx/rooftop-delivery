package com.rooftop.delivery.service;

/** Thrown when a run id does not exist. Mapped to 404 by the exception handler. */
public class RunNotFoundException extends RuntimeException {

    public RunNotFoundException(Long runId) {
        super("No delivery run with id " + runId);
    }
}
