package com.rooftop.delivery.service;

import com.rooftop.delivery.domain.CustomerOrder;
import com.rooftop.delivery.domain.DeliveryRun;
import com.rooftop.delivery.domain.Driver;
import com.rooftop.delivery.dto.CreateRunRequest;
import com.rooftop.delivery.dto.RunResponse;
import com.rooftop.delivery.engine.AssignmentResult;
import com.rooftop.delivery.engine.AssignmentResult.Assignment;
import com.rooftop.delivery.engine.DeliveryReport;
import com.rooftop.delivery.engine.DriverAssignmentEngine;
import com.rooftop.delivery.engine.OrderRequest;
import com.rooftop.delivery.repository.DeliveryRunRepository;
import java.util.List;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs the allocation and stores the outcome.
 *
 * <p>The engine decides who delivers what; this class only turns its answer into rows. A
 * run is allocated and written in a single transaction, so two concurrent requests cannot
 * interleave and give the same driver to both. Because drivers belong to a run, separate
 * runs never touch the same rows and no row locking is needed.
 *
 * <p>Entities are mapped to DTOs inside the transaction, so the web layer never touches a
 * lazy association.
 */
@Service
public class DeliveryRunService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryRunService.class);

    private final DriverAssignmentEngine engine;
    private final DeliveryRunRepository runRepository;

    public DeliveryRunService(DriverAssignmentEngine engine, DeliveryRunRepository runRepository) {
        this.engine = engine;
        this.runRepository = runRepository;
    }

    /**
     * Allocates the requested orders and saves the run.
     *
     * <p>Customers are numbered by their position in the request, matching C1..CN in the
     * problem statement.
     */
    @Transactional
    public RunResponse createRun(CreateRunRequest request) {
        List<OrderRequest> orders = numberCustomers(request);
        AssignmentResult result = engine.assign(request.driverCount(), orders);

        DeliveryRun run = new DeliveryRun(request.driverCount());
        result.drivers().forEach(state ->
                run.addDriver(new Driver(state.driverIndex(), state.availableAt())));

        // Drivers are flushed first so the orders below reference persisted rows.
        runRepository.saveAndFlush(run);

        for (Assignment assignment : result.assignments()) {
            OrderRequest order = orders.get(assignment.customerIndex() - 1);
            Driver driver = assignment.isAssigned() ? run.driverAt(assignment.driverIndex()) : null;
            run.addOrder(new CustomerOrder(
                    order.customerIndex(), order.orderTime(), order.travelTime(), driver));
        }
        runRepository.save(run);

        long assigned = result.assignments().stream().filter(Assignment::isAssigned).count();
        log.info("Run {}: assigned {} of {} orders over {} drivers",
                run.getId(), assigned, orders.size(), request.driverCount());

        return RunResponse.from(run);
    }

    @Transactional(readOnly = true)
    public RunResponse getRun(Long runId) {
        return RunResponse.from(load(runId));
    }

    /** The stored run in the exact text format required by the problem statement. */
    @Transactional(readOnly = true)
    public String report(Long runId) {
        return DeliveryReport.lines(load(runId).getOrders().stream()
                .map(order -> DeliveryReport.line(order.getCustomerIndex(), order.driverIndex()))
                .toList());
    }

    private DeliveryRun load(Long runId) {
        return runRepository.findById(runId).orElseThrow(() -> new RunNotFoundException(runId));
    }

    private List<OrderRequest> numberCustomers(CreateRunRequest request) {
        List<CreateRunRequest.Order> orders = request.orders();
        return IntStream.range(0, orders.size())
                .mapToObj(i -> new OrderRequest(
                        i + 1, orders.get(i).orderTime(), orders.get(i).travelTime()))
                .toList();
    }
}
