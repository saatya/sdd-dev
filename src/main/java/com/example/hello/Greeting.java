package com.example.hello;

import java.time.Instant;

/** Response body for GET /api/v1/hello (SPEC-001 §2, AC-5). */
public record Greeting(String message, Instant timestamp) {
}
