package com.rooftop.delivery.engine;

/** Whether a driver can take an order at a given minute. */
public enum DriverStatus {

    AVAILABLE("Available"),
    BUSY("Busy");

    private final String label;

    DriverStatus(String label) {
        this.label = label;
    }

    /** Wording used in the delivery report. */
    public String label() {
        return label;
    }
}
