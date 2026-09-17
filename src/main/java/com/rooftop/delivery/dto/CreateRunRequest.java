package com.rooftop.delivery.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request body for a new run, mirroring the input format of the problem statement:
 * N and M on the first line, then one (O, T) pair per customer.
 *
 * <p>Customers are numbered by position, so the first order is C1.
 */
public record CreateRunRequest(

        @Min(value = 0, message = "driverCount must be 0 or more")
        int driverCount,

        @NotEmpty(message = "at least one order is required")
        List<@Valid Order> orders) {

    public record Order(

            @Min(value = 0, message = "orderTime must be 0 or more")
            int orderTime,

            @Min(value = 0, message = "travelTime must be 0 or more")
            int travelTime) {
    }
}
