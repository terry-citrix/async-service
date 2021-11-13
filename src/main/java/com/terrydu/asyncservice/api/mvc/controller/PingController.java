package com.terrydu.asyncservice.api.mvc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/api/mvc/ping")
    public String ping() {
        return "pong";
    }
}
