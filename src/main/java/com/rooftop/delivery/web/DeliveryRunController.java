package com.rooftop.delivery.web;

import com.rooftop.delivery.dto.CreateRunRequest;
import com.rooftop.delivery.dto.RunResponse;
import com.rooftop.delivery.service.DeliveryRunService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP entry point. Holds no business logic: it validates the request and calls the service.
 *
 * <p>A run is created in one call because the allocation depends on the whole set of orders
 * and their order times, not on when requests happen to arrive. That also makes it
 * repeatable: posting the sample input twice gives the same answer twice.
 */
@RestController
@RequestMapping("/api/v1/delivery-runs")
public class DeliveryRunController {

    private final DeliveryRunService service;

    public DeliveryRunController(DeliveryRunService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RunResponse> create(@Valid @RequestBody CreateRunRequest request) {
        RunResponse run = service.createRun(request);
        return ResponseEntity
                .created(URI.create("/api/v1/delivery-runs/" + run.runId()))
                .body(run);
    }

    @GetMapping("/{runId}")
    public RunResponse get(@PathVariable Long runId) {
        return service.getRun(runId);
    }

    /** The run in the exact text format required by the problem statement. */
    @GetMapping(value = "/{runId}/report", produces = MediaType.TEXT_PLAIN_VALUE)
    public String report(@PathVariable Long runId) {
        return service.report(runId);
    }
}
