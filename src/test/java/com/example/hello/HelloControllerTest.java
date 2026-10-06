package com.example.hello;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** SPEC-001, AC-1 to AC-5. */
@WebMvcTest(HelloController.class)
@Import({GreetingService.class, ApiExceptionHandler.class})
class HelloControllerTest {

    private static final String ISO_UTC = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z$";

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("AC-1: no name returns Hello, World!")
    void defaultGreeting() throws Exception {
        mvc.perform(get("/api/v1/hello"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Hello, World!"));
    }

    @Test
    @DisplayName("AC-2: name is used in the greeting")
    void namedGreeting() throws Exception {
        mvc.perform(get("/api/v1/hello").param("name", "Satya"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, Satya!"));
    }

    @Test
    @DisplayName("AC-3: surrounding whitespace is trimmed")
    void trimsName() throws Exception {
        mvc.perform(get("/api/v1/hello").param("name", "  Mary-Jane  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, Mary-Jane!"));
    }

    @ParameterizedTest(name = "AC-4: rejects \"{0}\"")
    @ValueSource(strings = {
            "   ",
            "Robert'); DROP TABLE--",
            "<script>",
            "Name\nInjected",
            "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijX" // 51 chars
    })
    void rejectsInvalidNames(String name) throws Exception {
        mvc.perform(get("/api/v1/hello").param("name", name))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid name"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("AC-4: exactly 50 chars is accepted (boundary)")
    void acceptsFiftyChars() throws Exception {
        String fifty = "a".repeat(50);
        mvc.perform(get("/api/v1/hello").param("name", fifty))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, " + fifty + "!"));
    }

    @Test
    @DisplayName("AC-5: response has an ISO-8601 UTC timestamp")
    void hasTimestamp() throws Exception {
        mvc.perform(get("/api/v1/hello"))
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.timestamp").value(org.hamcrest.Matchers.matchesPattern(ISO_UTC)));
    }
}
