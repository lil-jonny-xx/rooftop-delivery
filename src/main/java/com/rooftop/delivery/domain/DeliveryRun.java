package com.rooftop.delivery.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * One allocation of N orders over M drivers.
 *
 * <p>Drivers and orders belong to a run rather than to the system as a whole. Without that
 * scope a second run would see the drivers left busy by the first one, so the same input
 * would produce a different answer.
 */
@Entity
@Table(name = "delivery_runs")
public class DeliveryRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "run_id")
    private Long id;

    @Column(name = "driver_count", nullable = false)
    private int driverCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("driverIndex")
    private List<Driver> drivers = new ArrayList<>();

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("customerIndex")
    private List<CustomerOrder> orders = new ArrayList<>();

    protected DeliveryRun() {
        // for JPA
    }

    public DeliveryRun(int driverCount) {
        this.driverCount = driverCount;
    }

    public void addDriver(Driver driver) {
        drivers.add(driver);
        driver.setRun(this);
    }

    public void addOrder(CustomerOrder order) {
        orders.add(order);
        order.setRun(this);
    }

    /** The driver numbered {@code driverIndex} in this run. */
    public Driver driverAt(int driverIndex) {
        return drivers.stream()
                .filter(driver -> driver.getDriverIndex() == driverIndex)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no driver " + driverIndex));
    }

    /**
     * Last minute an order was placed in this run, used as the moment driver status is
     * reported for. Zero when the run has no orders.
     */
    public int lastOrderTime() {
        return orders.stream().mapToInt(CustomerOrder::getOrderTime).max().orElse(0);
    }

    public Long getId() {
        return id;
    }

    public int getDriverCount() {
        return driverCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Driver> getDrivers() {
        return drivers;
    }

    public List<CustomerOrder> getOrders() {
        return orders;
    }
}
