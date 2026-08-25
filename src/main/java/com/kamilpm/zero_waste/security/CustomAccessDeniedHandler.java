package com.kamilpm.zero_waste.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.domain.response.ErrorResponse;

import io.lettuce.core.dynamic.annotation.CommandNaming;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException, ServletException {
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setStatus(HttpStatus.FORBIDDEN.value());

    ErrorResponse error = new ErrorResponse(
        "You do not have permission to perform this action",
        request.getRequestURI(),
        403,
        "forbidden");

    JsonMapper mapper = JsonMapper.builder().build();
    mapper.writeValue(response.getOutputStream(), error);

  }
}
