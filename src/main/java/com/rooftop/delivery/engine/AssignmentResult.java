package com.rooftop.delivery.engine;

import java.util.List;

/**
 * Outcome of allocating one batch of orders.
 *
 * @param assignments one entry per order, ordered by customer index
 * @param drivers     final state of each driver, ordered by driver index
 */
public record AssignmentResult(List<Assignment> assignments, List<DriverState> drivers) {

    public AssignmentResult(List<Assignment> assignments, List<DriverState> drivers) {
        this.assignments = List.copyOf(assignments);
        this.drivers = List.copyOf(drivers);
    }

    /**
     * Which driver took an order. {@code driverIndex} is null when no driver was free.
     */
    public record Assignment(int customerIndex, Integer driverIndex) {

        public boolean isAssigned() {
            return driverIndex != null;
        }
    }

    /** Where a driver ended up. Status is derived from availableAt, never stored. */
    public record DriverState(int driverIndex, int availableAt) {

        public DriverStatus statusAt(int minute) {
            return availableAt <= minute ? DriverStatus.AVAILABLE : DriverStatus.BUSY;
        }
    }
}
