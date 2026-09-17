package com.rooftop.delivery.web;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** End to end over the real stack: controller, service, engine, JPA, database. */
@SpringBootTest
@AutoConfigureMockMvc
class DeliveryRunControllerTest {

    /** The input from the problem statement: N = 6, M = 2. */
    private static final String SAMPLE_REQUEST = """
            {
              "driverCount": 2,
              "orders": [
                {"orderTime": 1,  "travelTime": 10},
                {"orderTime": 4,  "travelTime": 20},
                {"orderTime": 15, "travelTime": 5},
                {"orderTime": 22, "travelTime": 20},
                {"orderTime": 24, "travelTime": 10},
                {"orderTime": 25, "travelTime": 10}
              ]
            }
            """;

    private static final String EXPECTED_REPORT = String.join("\n",
            "C1 - D1",
            "C2 - D2",
            "C3 - D1",
            "C4 - D1",
            "C5 - D2",
            "C6 - No Food :-(");

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST allocates the sample input and stores it")
    void createsRunFromSampleInput() throws Exception {
        mockMvc.perform(post("/api/v1/delivery-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SAMPLE_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orders[0].driverIndex", is(1)))
                .andExpect(jsonPath("$.orders[1].driverIndex", is(2)))
                .andExpect(jsonPath("$.orders[2].driverIndex", is(1)))
                .andExpect(jsonPath("$.orders[3].driverIndex", is(1)))
                .andExpect(jsonPath("$.orders[4].driverIndex", is(2)))
                // A refused order carries no driverIndex at all, not a null one.
                .andExpect(jsonPath("$.orders[5].driverIndex").doesNotExist())
                .andExpect(jsonPath("$.orders[5].status", is("UNASSIGNED")))
                .andExpect(jsonPath("$.orders[5].message", is("No Food :-(")))
                .andExpect(jsonPath("$.drivers[0].availableAt", is(42)))
                .andExpect(jsonPath("$.drivers[1].availableAt", is(34)));
    }

    @Test
    @DisplayName("the stored run reads back in the required output format")
    void reportsStoredRunAsText() throws Exception {
        String runId = createSampleRun();

        mockMvc.perform(get("/api/v1/delivery-runs/{runId}/report", runId))
                .andExpect(status().isOk())
                .andExpect(content().string(EXPECTED_REPORT));
    }

    @Test
    @DisplayName("two identical runs give the same answer")
    void runsAreIndependent() throws Exception {
        createSampleRun();
        String secondRunId = createSampleRun();

        // Drivers belong to a run, so the second run does not inherit busy drivers.
        mockMvc.perform(get("/api/v1/delivery-runs/{runId}/report", secondRunId))
                .andExpect(content().string(EXPECTED_REPORT));
    }

    @Test
    @DisplayName("an unknown run id is a 404")
    void unknownRunIsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/delivery-runs/{runId}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("an empty order list is rejected")
    void emptyOrderListIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/delivery-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"driverCount\": 2, \"orders\": []}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]", is("orders: at least one order is required")));
    }

    @Test
    @DisplayName("a negative travel time is rejected")
    void negativeTravelTimeIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/delivery-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"driverCount\": 1, \"orders\": [{\"orderTime\": 1, "
                                + "\"travelTime\": -5}]}"))
                .andExpect(status().isBadRequest());
    }

    private String createSampleRun() throws Exception {
        String body = mockMvc.perform(post("/api/v1/delivery-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SAMPLE_REQUEST))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(body, "$.runId").toString();
    }
}
