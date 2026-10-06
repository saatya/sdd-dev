package com.example.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Implements openapi/hello-api.yaml operation getGreeting. */
@RestController
@RequestMapping("/api/v1")
public class HelloController {

    private final GreetingService greetingService;

    public HelloController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/hello")
    public Greeting hello(@RequestParam(name = "name", required = false) String name) {
        return greetingService.greet(name);
    }
}
