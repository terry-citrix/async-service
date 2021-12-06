package com.terrydu.asyncservice.api.mvc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * http://localhost:8083/api/mvc/ping
 */
@RestController
public class PingController {

  /**
   * NOTE: This path mapping is relative to "/api/mvc"
   */
  @GetMapping("/ping")
  public String ping() {
    return "pong";
  }
}
