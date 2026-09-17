package com.rooftop.delivery.domain;

import com.rooftop.delivery.engine.DriverStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A delivery boy inside one run, numbered D1..DM by {@code driverIndex}.
 *
 * <p>Only the minute the driver is next free is stored. Status is computed from it, so
 * there is one source of truth and no row can be left saying BUSY after the delivery ended.
 */
@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "driver_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private DeliveryRun run;

    @Column(name = "driver_index", nullable = false)
    private int driverIndex;

    @Column(name = "available_at", nullable = false)
    private int availableAt;

    protected Driver() {
        // for JPA
    }

    public Driver(int driverIndex, int availableAt) {
        this.driverIndex = driverIndex;
        this.availableAt = availableAt;
    }

    public DriverStatus statusAt(int minute) {
        return availableAt <= minute ? DriverStatus.AVAILABLE : DriverStatus.BUSY;
    }

    public String label() {
        return "D" + driverIndex;
    }

    void setRun(DeliveryRun run) {
        this.run = run;
    }

    public Long getId() {
        return id;
    }

    public int getDriverIndex() {
        return driverIndex;
    }

    public int getAvailableAt() {
        return availableAt;
    }
}
