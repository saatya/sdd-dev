package com.example.hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

/** Business rules for SPEC-001 (AC-1 to AC-7). */
@Service
public class GreetingService {

    private static final Logger log = LoggerFactory.getLogger(GreetingService.class);

    static final String DEFAULT_NAME = "World";
    static final int MAX_LENGTH = 50;
    private static final Pattern ALLOWED = Pattern.compile("^[A-Za-z' -]+$");

    private final Clock clock;

    public GreetingService() {
        this(Clock.systemUTC());
    }

    GreetingService(Clock clock) {
        this.clock = clock;
    }

    public Greeting greet(String rawName) {
        String name = resolveName(rawName);
        // AC-6: one INFO line per successful greeting. The name is safe to log
        // because it has already passed the allow-list above.
        log.info("greeting served name={}", name);
        return new Greeting("Hello, " + name + "!", Instant.now(clock).truncatedTo(ChronoUnit.MILLIS));
    }

    private String resolveName(String rawName) {
        if (rawName == null) {
            return DEFAULT_NAME;                                  // AC-1
        }
        String name = rawName.strip();                            // AC-3
        if (name.isEmpty() || name.length() > MAX_LENGTH || !ALLOWED.matcher(name).matches()) {
            // AC-7: never log the rejected value (log-injection risk).
            log.warn("greeting rejected reason=invalid_name length={}", rawName.length());
            throw new InvalidNameException();                     // AC-4
        }
        return name;
    }
}
