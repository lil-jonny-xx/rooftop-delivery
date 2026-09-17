package com.rooftop.delivery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * An order placed by customer C{customerIndex} inside one run.
 *
 * <p>{@code driver} is null exactly when the order was refused, which is the row the report
 * prints as "No Food :-(".
 */
@Entity
@Table(name = "customer_orders")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private DeliveryRun run;

    @Column(name = "customer_index", nullable = false)
    private int customerIndex;

    @Column(name = "order_time", nullable = false)
    private int orderTime;

    @Column(name = "travel_time", nullable = false)
    private int travelTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    protected CustomerOrder() {
        // for JPA
    }

    public CustomerOrder(int customerIndex, int orderTime, int travelTime, Driver driver) {
        this.customerIndex = customerIndex;
        this.orderTime = orderTime;
        this.travelTime = travelTime;
        this.driver = driver;
        this.status = driver == null ? OrderStatus.UNASSIGNED : OrderStatus.ASSIGNED;
    }

    /** Driver number for the report, null when the order was refused. */
    public Integer driverIndex() {
        return driver == null ? null : driver.getDriverIndex();
    }

    void setRun(DeliveryRun run) {
        this.run = run;
    }

    public Long getId() {
        return id;
    }

    public int getCustomerIndex() {
        return customerIndex;
    }

    public int getOrderTime() {
        return orderTime;
    }

    public int getTravelTime() {
        return travelTime;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
