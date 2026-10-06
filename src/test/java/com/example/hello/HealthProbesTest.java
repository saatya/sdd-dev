package com.example.hello;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** SPEC-001, AC-8: Kubernetes probe endpoints are exposed and UP. */
@SpringBootTest
@AutoConfigureMockMvc
class HealthProbesTest {

    @Autowired
    private MockMvc mvc;

    @ParameterizedTest(name = "AC-8: {0} is UP")
    @ValueSource(strings = {"/actuator/health/liveness", "/actuator/health/readiness"})
    void probeIsUp(String path) throws Exception {
        mvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
