package com.rooftop.delivery.repository;

import com.rooftop.delivery.domain.DeliveryRun;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Runs are the only aggregate root: drivers and orders are reached through their run and
 * cascade-saved with it, so one repository is enough.
 */
public interface DeliveryRunRepository extends JpaRepository<DeliveryRun, Long> {
}
