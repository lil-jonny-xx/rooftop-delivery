package com.rooftop.delivery.engine;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Renders the output text required by the problem statement.
 *
 * <pre>
 * C1 - D1
 * C6 - No Food :-(
 * </pre>
 *
 * <p>The wording lives here only, so the REST layer and the tests cannot drift apart.
 */
public final class DeliveryReport {

    public static final String NO_FOOD = "No Food :-(";

    private DeliveryReport() {
    }

    /** One line per order, C1 to CN. */
    public static String of(AssignmentResult result) {
        return lines(result.assignments().stream()
                .map(a -> line(a.customerIndex(), a.driverIndex()))
                .toList());
    }

    /** A single order line. {@code driverIndex} is null for a refused order. */
    public static String line(int customerIndex, Integer driverIndex) {
        String outcome = driverIndex == null ? NO_FOOD : "D" + driverIndex;
        return "C" + customerIndex + " - " + outcome;
    }

    /** Joins already rendered lines, used when the lines come from stored orders. */
    public static String lines(List<String> lines) {
        return lines.stream().collect(Collectors.joining("\n"));
    }
}
