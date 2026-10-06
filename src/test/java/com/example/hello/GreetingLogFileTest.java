package com.example.hello;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SPEC-001, AC-6 and AC-7: greetings are logged to the file the sidecar tails.
 * Uses the same property (logging.file.name) the Deployment sets via env var.
 */
@SpringBootTest(properties = "logging.file.name=" + GreetingLogFileTest.LOG_FILE)
@AutoConfigureMockMvc
class GreetingLogFileTest {

    static final String LOG_FILE = "target/test-logs/app.log";

    @Autowired
    private MockMvc mvc;

    @BeforeAll
    static void cleanLogFile() throws IOException {
        Files.deleteIfExists(Path.of(LOG_FILE));
    }

    @Test
    @DisplayName("AC-6: successful greeting is written to the log file")
    void logsSuccessfulGreeting() throws Exception {
        mvc.perform(get("/api/v1/hello").param("name", "Sidecar")).andExpect(status().isOk());

        assertThat(readLog())
                .contains("INFO")
                .contains("greeting served name=Sidecar");
    }

    @Test
    @DisplayName("AC-7: rejected input is logged as WARN without the raw value")
    void logsRejectionWithoutValue() throws Exception {
        String hostile = "evil<payload>";
        mvc.perform(get("/api/v1/hello").param("name", hostile)).andExpect(status().isBadRequest());

        assertThat(readLog())
                .contains("WARN")
                .contains("greeting rejected")
                .doesNotContain(hostile);
    }

    private static String readLog() throws IOException {
        Path path = Path.of(LOG_FILE);
        assertThat(path).as("log file must exist at %s", LOG_FILE).exists();
        return Files.readString(path);
    }
}
