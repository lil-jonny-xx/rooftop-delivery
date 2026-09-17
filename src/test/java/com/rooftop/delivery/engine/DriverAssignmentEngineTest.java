package com.rooftop.delivery.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rooftop.delivery.engine.AssignmentResult.DriverState;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DriverAssignmentEngineTest {

    private static final List<OrderRequest> SAMPLE = List.of(
            new OrderRequest(1, 1, 10),
            new OrderRequest(2, 4, 20),
            new OrderRequest(3, 15, 5),
            new OrderRequest(4, 22, 20),
            new OrderRequest(5, 24, 10),
            new OrderRequest(6, 25, 10));

    private static final String EXPECTED_SAMPLE_OUTPUT = String.join("\n",
            "C1 - D1",
            "C2 - D2",
            "C3 - D1",
            "C4 - D1",
            "C5 - D2",
            "C6 - No Food :-(");

    private final DriverAssignmentEngine engine = new DriverAssignmentEngine();

    @Test
    @DisplayName("produces the expected output for the sample input")
    void sampleInput() {
        AssignmentResult result = engine.assign(2, SAMPLE);

        assertThat(DeliveryReport.of(result)).isEqualTo(EXPECTED_SAMPLE_OUTPUT);
        assertThat(result.drivers()).containsExactly(
                new DriverState(1, 42),
                new DriverState(2, 34));
    }

    @Test
    @DisplayName("gives the order to the lowest free driver")
    void lowestIndexWins() {
        AssignmentResult result = engine.assign(3, List.of(new OrderRequest(1, 0, 5)));

        assertThat(result.assignments().get(0).driverIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("moves to the next driver while the first is out")
    void nextDriverWhileBusy() {
        AssignmentResult result = engine.assign(2, List.of(
                new OrderRequest(1, 0, 10),
                new OrderRequest(2, 1, 10)));

        assertThat(result.assignments().get(1).driverIndex()).isEqualTo(2);
    }

    @Test
    @DisplayName("reuses a driver who is already back")
    void reusesReturnedDriver() {
        AssignmentResult result = engine.assign(2, List.of(
                new OrderRequest(1, 0, 5),
                new OrderRequest(2, 30, 5)));

        assertThat(result.assignments().get(1).driverIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("a driver returning exactly at the order time is free")
    void returnsExactlyAtOrderTime() {
        AssignmentResult result = engine.assign(1, List.of(
                new OrderRequest(1, 0, 20),
                new OrderRequest(2, 20, 5)));

        assertThat(result.assignments().get(1).driverIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("refuses the order when every driver is out")
    void refusesWhenAllBusy() {
        AssignmentResult result = engine.assign(2, List.of(
                new OrderRequest(1, 0, 50),
                new OrderRequest(2, 0, 50),
                new OrderRequest(3, 1, 5)));

        assertThat(result.assignments().get(2).isAssigned()).isFalse();
        assertThat(DeliveryReport.line(3, null)).isEqualTo("C3 - No Food :-(");
    }

    @Test
    @DisplayName("a refused order does not change driver state")
    void refusalLeavesStateAlone() {
        AssignmentResult result = engine.assign(1, List.of(
                new OrderRequest(1, 0, 50),
                new OrderRequest(2, 1, 5)));

        assertThat(result.drivers()).containsExactly(new DriverState(1, 50));
    }

    @Test
    @DisplayName("refuses everything when there are no drivers")
    void noDrivers() {
        AssignmentResult result = engine.assign(0, List.of(new OrderRequest(1, 0, 5)));

        assertThat(result.assignments().get(0).isAssigned()).isFalse();
        assertThat(result.drivers()).isEmpty();
    }

    @Test
    @DisplayName("input order does not matter, output stays C1..CN")
    void sortsBeforeProcessing() {
        List<OrderRequest> shuffled = List.of(
                new OrderRequest(4, 22, 20),
                new OrderRequest(1, 1, 10),
                new OrderRequest(6, 25, 10),
                new OrderRequest(3, 15, 5),
                new OrderRequest(5, 24, 10),
                new OrderRequest(2, 4, 20));

        assertThat(DeliveryReport.of(engine.assign(2, shuffled)))
                .isEqualTo(EXPECTED_SAMPLE_OUTPUT);
    }

    @Test
    @DisplayName("two orders in the same minute go to the lower customer first")
    void tieOnOrderTime() {
        AssignmentResult result = engine.assign(1, List.of(
                new OrderRequest(2, 10, 5),
                new OrderRequest(1, 10, 5)));

        assertThat(result.assignments().get(0).driverIndex()).isEqualTo(1);
        assertThat(result.assignments().get(1).isAssigned()).isFalse();
    }

    @Test
    @DisplayName("rejects a negative driver count")
    void rejectsNegativeDriverCount() {
        assertThatThrownBy(() -> engine.assign(-1, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("driverCount");
    }

    @Test
    @DisplayName("handles a fleet too large to scan per order")
    void largeFleet() {
        int size = 100_000;
        List<OrderRequest> orders = IntStream.rangeClosed(1, size)
                .mapToObj(i -> new OrderRequest(i, 0, 1_000))
                .toList();

        AssignmentResult result = engine.assign(size, orders);

        assertThat(result.assignments()).allMatch(AssignmentResult.Assignment::isAssigned);
        assertThat(result.assignments().get(size - 1).driverIndex()).isEqualTo(size);
    }
}
