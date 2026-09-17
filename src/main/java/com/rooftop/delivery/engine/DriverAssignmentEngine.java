package com.rooftop.delivery.engine;

import com.rooftop.delivery.engine.AssignmentResult.Assignment;
import com.rooftop.delivery.engine.AssignmentResult.DriverState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import org.springframework.stereotype.Component;

/**
 * Allocates delivery boys to customer orders.
 *
 * <p>Business rules:
 * <ol>
 *   <li>Orders are handled in ascending order time; a tie goes to the lower customer index.</li>
 *   <li>A driver is free for an order when {@code availableAt <= orderTime}.</li>
 *   <li>Of the free drivers the lowest index always wins.</li>
 *   <li>An assigned driver is free again at {@code orderTime + travelTime}.</li>
 *   <li>If nobody is free the order is refused and no driver state changes.</li>
 * </ol>
 *
 * <p>Two heaps are used instead of scanning the fleet per order: one of free drivers ordered
 * by index, one of busy drivers ordered by return time. That gives O(N log N + N log M)
 * against O(N x M) for a scan, which matters once the fleet is large.
 *
 * <p>No clock, no database, no state between calls, so a run is reproducible and the rules
 * can be tested without starting the application.
 */
@Component
public class DriverAssignmentEngine {

    private record BusyDriver(int driverIndex, int availableAt) {
    }

    private static final Comparator<OrderRequest> PROCESSING_ORDER =
            Comparator.comparingInt(OrderRequest::orderTime)
                    .thenComparingInt(OrderRequest::customerIndex);

    private static final Comparator<BusyDriver> BY_RETURN_TIME =
            Comparator.comparingInt(BusyDriver::availableAt)
                    .thenComparingInt(BusyDriver::driverIndex);

    /**
     * @param driverCount number of delivery boys, M; they are numbered 1..M
     * @param orders      the orders to allocate, in any input order
     */
    public AssignmentResult assign(int driverCount, List<OrderRequest> orders) {
        if (driverCount < 0) {
            throw new IllegalArgumentException("driverCount must be >= 0: " + driverCount);
        }

        List<OrderRequest> pending = new ArrayList<>(orders);
        pending.sort(PROCESSING_ORDER);

        Queue<Integer> free = new PriorityQueue<>(Math.max(1, driverCount));
        Queue<BusyDriver> busy = new PriorityQueue<>(Math.max(1, driverCount), BY_RETURN_TIME);
        for (int driverIndex = 1; driverIndex <= driverCount; driverIndex++) {
            free.add(driverIndex);
        }

        int[] availableAt = new int[driverCount];
        List<Assignment> assignments = new ArrayList<>(pending.size());

        for (OrderRequest order : pending) {
            releaseReturnedDrivers(busy, free, order.orderTime());

            Integer driverIndex = free.poll();
            if (driverIndex == null) {
                assignments.add(new Assignment(order.customerIndex(), null));
                continue;
            }
            busy.add(new BusyDriver(driverIndex, order.freeAt()));
            availableAt[driverIndex - 1] = order.freeAt();
            assignments.add(new Assignment(order.customerIndex(), driverIndex));
        }

        assignments.sort(Comparator.comparingInt(Assignment::customerIndex));
        return new AssignmentResult(assignments, driverStates(availableAt));
    }

    /**
     * Moves every driver back at the restaurant by {@code minute} into the free heap.
     *
     * <p>A driver returning exactly at {@code minute} counts as free. Dropping that equality
     * breaks the expected output: C5 is placed at minute 24 and D2 returns at minute 24.
     */
    private void releaseReturnedDrivers(Queue<BusyDriver> busy, Queue<Integer> free, int minute) {
        while (!busy.isEmpty() && busy.peek().availableAt() <= minute) {
            free.add(busy.poll().driverIndex());
        }
    }

    private List<DriverState> driverStates(int[] availableAt) {
        List<DriverState> drivers = new ArrayList<>(availableAt.length);
        for (int i = 0; i < availableAt.length; i++) {
            drivers.add(new DriverState(i + 1, availableAt[i]));
        }
        return drivers;
    }
}
