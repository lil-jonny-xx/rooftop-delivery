package com.rooftop.delivery.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rooftop.delivery.domain.CustomerOrder;
import com.rooftop.delivery.domain.DeliveryRun;
import com.rooftop.delivery.domain.OrderStatus;
import com.rooftop.delivery.engine.DeliveryReport;
import com.rooftop.delivery.engine.DriverStatus;
import java.util.List;

/**
 * A run as returned by the API.
 *
 * <p>Driver status is reported as at the last order time of the run, since that is the
 * moment the allocation describes.
 */
public record RunResponse(
        Long runId, int driverCount, List<OrderView> orders, List<DriverView> drivers) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OrderView(
            int customerIndex,
            int orderTime,
            int travelTime,
            Integer driverIndex,
            OrderStatus status,
            String message) {
    }

    public record DriverView(int driverIndex, int availableAt, DriverStatus status) {
    }

    public static RunResponse from(DeliveryRun run) {
        int reportedAt = run.lastOrderTime();

        List<OrderView> orders = run.getOrders().stream()
                .map(RunResponse::toOrderView)
                .toList();

        List<DriverView> drivers = run.getDrivers().stream()
                .map(driver -> new DriverView(
                        driver.getDriverIndex(),
                        driver.getAvailableAt(),
                        driver.statusAt(reportedAt)))
                .toList();

        return new RunResponse(run.getId(), run.getDriverCount(), orders, drivers);
    }

    private static OrderView toOrderView(CustomerOrder order) {
        boolean refused = order.getStatus() == OrderStatus.UNASSIGNED;
        return new OrderView(
                order.getCustomerIndex(),
                order.getOrderTime(),
                order.getTravelTime(),
                order.driverIndex(),
                order.getStatus(),
                refused ? DeliveryReport.NO_FOOD : null);
    }
}
