package com.rooftop.delivery.engine;

/**
 * One customer order handed to the assignment engine.
 *
 * <p>Times are whole minutes from the start of the delivery window, matching the integer
 * O and T values in the problem statement.
 */
public record OrderRequest(int customerIndex, int orderTime, int travelTime) {

    public OrderRequest {
        if (customerIndex < 1) {
            throw new IllegalArgumentException("customerIndex must be >= 1: " + customerIndex);
        }
        if (orderTime < 0 || travelTime < 0) {
            throw new IllegalArgumentException("orderTime and travelTime must be >= 0");
        }
    }

    /** Minute the driver who takes this order is free again. */
    public int freeAt() {
        return orderTime + travelTime;
    }
}
