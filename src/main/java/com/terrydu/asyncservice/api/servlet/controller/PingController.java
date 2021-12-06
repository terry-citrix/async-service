package com.terrydu.asyncservice.api.servlet.controller;

import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * http://localhost:8083/api/servlet/ping
 */
@WebServlet(name = "PingServlet", urlPatterns = "/api/servlet/ping")
public class PingController extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {
    try {
      ServletOutputStream out = servletResponse.getOutputStream();
      servletResponse.setStatus(HttpServletResponse.SC_OK);
      servletResponse.setContentType("application/json");
      servletResponse.setHeader("Cache-Control", "no-cache");
      out.println("pong");
      out.close();

    } catch (Exception e) {
      servletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
