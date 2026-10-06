package com.example.hello;

/** Thrown when the name parameter breaks the rules in SPEC-001 §2 (AC-4). */
public class InvalidNameException extends RuntimeException {

    public static final String DETAIL =
            "name must be 1-50 characters: letters, spaces, hyphens or apostrophes";

    public InvalidNameException() {
        super(DETAIL);
    }
}
